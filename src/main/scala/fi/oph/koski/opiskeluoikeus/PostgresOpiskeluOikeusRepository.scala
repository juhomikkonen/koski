package fi.oph.koski.opiskeluoikeus

import java.sql.SQLException

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.jsonMethods._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{KoskiHenkilöCache, KoskiHenkilöCacheUpdater, PossiblyUnverifiedHenkilöOid}
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusChangeValidator.validateOpiskeluoikeusChange
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryFilter._
import fi.oph.koski.schema.Opiskeluoikeus.VERSIO_1
import fi.oph.koski.schema._
import fi.oph.koski.servlet.InvalidRequestException
import fi.oph.koski.util.{PaginationSettings, QueryPagination, ReactiveStreamsToRx}
import org.json4s.JArray
import rx.lang.scala.Observable
import slick.dbio.Effect.{Read, Transactional, Write}
import slick.dbio.NoStream
import slick.lifted.Query
import slick.{dbio, lifted}

class PostgresOpiskeluoikeusRepository(val db: DB, historyRepository: OpiskeluoikeusHistoryRepository, henkilöCache: KoskiHenkilöCacheUpdater) extends OpiskeluoikeusRepository with GlobalExecutionContext with KoskiDatabaseMethods with Logging with SerializableTransactions {
  override def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession) = {
    val query: lifted.Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq] = for {
      oo <- OpiskeluOikeudetWithAccessCheck
      if oo.oppijaOid inSetBind oppijat.map(_.oid)
    } yield {
      oo
    }

    //logger.info(query.result.statements.head)

    val oppijatJoillaOpiskeluoikeuksia: Set[String] = runDbSync(query.map(_.oppijaOid).result).toSet

    oppijat.filter { oppija => oppijatJoillaOpiskeluoikeuksia.contains(oppija.oid)}
  }


  override def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = {
    runDbSync(findByOppijaOidAction(oid).map(rows => rows.map(_.toOpiskeluoikeus)))
  }

  override def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus] = {
    assert(oid == user.oid, "Käyttäjän oid: " + user.oid + " poikkeaa etsittävän oppijan oidista: " + oid)
    runDbSync(findAction(OpiskeluOikeudet.filter(_.oppijaOid === oid)).map(rows => rows.map(_.toOpiskeluoikeus)))
  }

  def findById(id: Int)(implicit user: KoskiSession): Option[OpiskeluoikeusRow] = {
    runDbSync(findAction(OpiskeluOikeudetWithAccessCheck.filter(_.id === id))).headOption
  }

  def delete(id: Int)(implicit user: KoskiSession): HttpStatus = {
    runDbSync(OpiskeluOikeudetWithAccessCheck.filter(_.id === id).delete) match {
      case 0 => KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
      case 1 => HttpStatus.ok
      case _ => KoskiErrorCategory.internalError()
    }
  }

  override def streamingQuery(filters: List[OpiskeluoikeusQueryFilter], sorting: Option[OpiskeluoikeusSortOrder], pagination: Option[PaginationSettings])(implicit user: KoskiSession): Observable[(OpiskeluoikeusRow, HenkilöRow)] = {
    import ReactiveStreamsToRx._

    val query = filters.foldLeft(OpiskeluOikeudetWithAccessCheck.asInstanceOf[Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq]] join Tables.Henkilöt on (_.oppijaOid === _.oid)) {
      case (query, OpiskeluoikeusPäättynytAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusPäättynytViimeistään(päivä)) => query.filter(_._1.data.#>>(List("päättymispäivä")) <= päivä.toString)
      case (query, OpiskeluoikeusAlkanutAikaisintaan(päivä)) => query.filter(_._1.data.#>>(List("alkamispäivä")) >= päivä.toString)
      case (query, OpiskeluoikeusAlkanutViimeistään(päivä)) => query.filter(_._1.data.#>>(List("alkamispäivä")) <= päivä.toString)
      case (query, SuorituksenTila(tila)) => query.filter(_._1.data.+>("suoritukset").@>(parse(s"""[{"tila":{"koodiarvo":"${tila.koodiarvo}"}}]""")))
      case (query, OpiskeluoikeudenTyyppi(tyyppi)) => query.filter(_._1.data.#>>(List("tyyppi", "koodiarvo")) === tyyppi.koodiarvo)
      case (query, SuorituksenTyyppi(tyyppi)) => query.filter(_._1.data.+>("suoritukset").@>(parse(s"""[{"tyyppi":{"koodiarvo":"${tyyppi.koodiarvo}"}}]""")))
      case (query, OpiskeluoikeudenTila(tila)) => query.filter(_._1.data.#>>(List("tila", "opiskeluoikeusjaksot", "-1", "tila", "koodiarvo")) === tila.koodiarvo)
      case (query, Tutkintohaku(tutkinnot, osaamisalat, nimikkeet)) =>
        val matchers = tutkinnot.map { tutkinto =>
          parse(s"""[{"koulutusmoduuli":{"tunniste": {"koodiarvo": "${tutkinto.koodiarvo}"}}}]""")
        } ++ nimikkeet.map { nimike =>
          parse(s"""[{"tutkintonimike":[{"koodiarvo": "${nimike.koodiarvo}"}]}]""")
        } ++ osaamisalat.map { osaamisala =>
          parse(s"""[{"osaamisala":[{"koodiarvo": "${osaamisala.koodiarvo}"}]}]""")
        }
        query.filter(_._1.data.+>("suoritukset").@>(matchers.bind.any))
      case (query, OpiskeluoikeusQueryFilter.Toimipiste(toimipisteet)) =>
        val matchers = toimipisteet.map { toimipiste =>
          parse(s"""[{"toimipiste":{"oid": "${toimipiste.oid}"}}]""")
        }
        query.filter(_._1.data.+>("suoritukset").@>(matchers.bind.any))
      case (query, Luokkahaku(hakusana)) =>
        query.filter({ case t: (Tables.OpiskeluoikeusTable, Tables.HenkilöTable) => t._1.luokka ilike (hakusana + "%")})
      case (query, Nimihaku(hakusana)) =>
        query.filter{ case (_, henkilö) =>
          KoskiHenkilöCache.filterByQuery(hakusana)(henkilö)
        }
      case (query, SuoritusJsonHaku(json)) => query.filter(_._1.data.+>("suoritukset").@>(json))
      case (query, filter) => throw new InvalidRequestException(KoskiErrorCategory.internalError("Hakua ei ole toteutettu: " + filter))
    }

    def ap(tuple: (OpiskeluoikeusTable, HenkilöTable)) = tuple._1.data.#>>(List("alkamispäivä"))
    def luokka(tuple: (OpiskeluoikeusTable, HenkilöTable)) = tuple._1.luokka
    def nimi(tuple: (OpiskeluoikeusTable, HenkilöTable)) = (tuple._2.sukunimi.toLowerCase, tuple._2.etunimet.toLowerCase)
    def nimiDesc(tuple: (OpiskeluoikeusTable, HenkilöTable)) = (tuple._2.sukunimi.toLowerCase.desc, tuple._2.etunimet.toLowerCase.desc)

    val sorted = sorting match {
      case None => query
      case Some(Ascending(OpiskeluoikeusSortOrder.oppijaOid)) => query.sortBy(_._2.oid)
      case Some(Ascending("nimi")) => query.sortBy(nimi)
      case Some(Descending("nimi")) => query.sortBy(nimiDesc)
      case Some(Ascending("alkamispäivä")) => query.sortBy(tuple => (ap(tuple), nimi(tuple)))
      case Some(Descending("alkamispäivä")) => query.sortBy(tuple => (ap(tuple).desc, nimiDesc(tuple)))
      case Some(Ascending("luokka")) => query.sortBy(tuple => (luokka(tuple), nimi(tuple)))
      case Some(Descending("luokka")) => query.sortBy(tuple => (luokka(tuple).desc, nimiDesc(tuple)))
      case s => throw new InvalidRequestException(KoskiErrorCategory.badRequest.queryParam("Epäkelpo järjestyskriteeri: " + s))
    }

    val paginated = QueryPagination.applyPagination(sorted, pagination)

    // Note: it won't actually stream unless you use both `transactionally` and `fetchSize`. It'll collect all the data into memory.
    db.stream(paginated.result.transactionally.withStatementParameters(fetchSize = 1000)).publish.refCount
  }


  override def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult] = {
    try {
      runDbSync(createOrUpdateAction(oppijaOid, opiskeluoikeus).transactionally)
    } catch {
      case e:SQLException if e.getSQLState == "23505" =>
        // 23505 = Unique constraint violation
        Left(KoskiErrorCategory.conflict.samanaikainenPäivitys())
    }
  }

  private def findByOppijaOidAction(oid: String)(implicit user: KoskiSession): dbio.DBIOAction[Seq[OpiskeluoikeusRow], NoStream, Read] = {
    findAction(OpiskeluOikeudetWithAccessCheck.filter(_.oppijaOid === oid))
  }

  private def findByIdentifierAction(identifier: OpiskeluoikeusIdentifier)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, Option[OpiskeluoikeusRow]], NoStream, Read] = identifier match{
    case PrimaryKey(id) => {
      findAction(OpiskeluOikeudetWithAccessCheck.filter(_.id === id)).map { rows =>
        rows.headOption match {
          case Some(oikeus) => Right(Some(oikeus))
          case None => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + id + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }
      }
    }

    case OppijaOidJaLähdejärjestelmänId(oppijaOid, lähdejärjestelmäId) => {
      findUnique(oppijaOid, { row =>
        row.toOpiskeluoikeus.lähdejärjestelmänId == Some(lähdejärjestelmäId)
      })
    }

    case i:OppijaOidOrganisaatioJaTyyppi => {
      findUnique(i.oppijaOid, { row =>
        OppijaOidOrganisaatioJaTyyppi(i.oppijaOid, row.toOpiskeluoikeus.oppilaitos.oid, row.toOpiskeluoikeus.tyyppi.koodiarvo, row.toOpiskeluoikeus.lähdejärjestelmänId) == identifier
      })
    }
  }

  private def findUnique(oppijaOid: String, f: OpiskeluoikeusRow => Boolean)(implicit user: KoskiSession) = {
    findByOppijaOidAction(oppijaOid).map(_.filter(f).toList).map {
      case List(singleRow) => Right(Some(singleRow))
      case Nil => Right(None)
      case multipleRows => Left(KoskiErrorCategory.internalError(s"Löytyi enemmän kuin yksi rivi päivitettäväksi (${multipleRows.map(_.id)})"))
    }
  }

  private def findAction(query: Query[OpiskeluoikeusTable, OpiskeluoikeusRow, Seq])(implicit user: KoskiSession): dbio.DBIOAction[Seq[OpiskeluoikeusRow], NoStream, Read] = {
    query.result
  }

  private def createOrUpdateAction(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Read with Write with Transactional] = {
    findByIdentifierAction(OpiskeluoikeusIdentifier(oppijaOid.oppijaOid, opiskeluoikeus)).flatMap { rows: Either[HttpStatus, Option[OpiskeluoikeusRow]] =>
      rows match {
        case Right(Some(vanhaOpiskeluoikeus)) =>
          updateAction(vanhaOpiskeluoikeus, opiskeluoikeus)
        case Right(None) =>
          oppijaOid.verified match {
            case Some(henkilö) =>
              henkilöCache.addHenkilöAction(henkilö).flatMap { _ =>
                createAction(henkilö.oid, opiskeluoikeus)
              }
            case None => DBIO.successful(Left(KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.oppijaOid + " ei löydy.")))
          }
        case Left(err) => DBIO.successful(Left(err))
      }
    }
  }

  private def createAction(oppijaOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    opiskeluoikeus.versionumero match {
      case Some(versio) if (versio != VERSIO_1) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero(s"Uudelle opiskeluoikeudelle annettu versionumero $versio")))
      case _ =>
        val tallennettavaOpiskeluoikeus = opiskeluoikeus.withIdAndVersion(id = None, versionumero = None)
        for {
          opiskeluoikeusId <- Tables.OpiskeluOikeudet.returning(OpiskeluOikeudet.map(_.id)) += Tables.OpiskeluoikeusTable.makeInsertableRow(oppijaOid, tallennettavaOpiskeluoikeus)
          diff = Json.toJValue(List(Map("op" -> "add", "path" -> "", "value" -> tallennettavaOpiskeluoikeus)))
          _ <- historyRepository.createAction(opiskeluoikeusId, VERSIO_1, user.oid, diff)
        } yield {
          Right(Created(opiskeluoikeusId, VERSIO_1, diff))
        }
    }
  }

  private def updateAction[A <: PäätasonSuoritus](oldRow: OpiskeluoikeusRow, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): dbio.DBIOAction[Either[HttpStatus, CreateOrUpdateResult], NoStream, Write] = {
    val (id, versionumero) = (oldRow.id, oldRow.versionumero)
    val nextVersionumero = versionumero + 1

    uusiOpiskeluoikeus.versionumero match {
      case Some(requestedVersionumero) if (requestedVersionumero != versionumero) =>
        DBIO.successful(Left(KoskiErrorCategory.conflict.versionumero("Annettu versionumero " + requestedVersionumero + " <> " + versionumero)))
      case _ =>
        val vanhaOpiskeluoikeus = oldRow.toOpiskeluoikeus

        val täydennettyOpiskeluoikeus = OpiskeluoikeusChangeMigrator.kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus).withVersion(nextVersionumero)

        val updatedValues@(newData, _, _, _) = Tables.OpiskeluoikeusTable.updatedFieldValues(täydennettyOpiskeluoikeus)

        val diff: JArray = Json.jsonDiff(oldRow.data, newData)
        diff.values.length match {
          case 0 =>
            DBIO.successful(Right(NotChanged(id, versionumero, diff)))
          case _ =>
            validateOpiskeluoikeusChange(vanhaOpiskeluoikeus, täydennettyOpiskeluoikeus) match {
              case HttpStatus.ok =>
                for {
                  rowsUpdated <- OpiskeluOikeudetWithAccessCheck.filter(_.id === id).map(_.updateableFields).update(updatedValues)
                  _ <- historyRepository.createAction(id, nextVersionumero, user.oid, diff)
                } yield {
                  rowsUpdated match {
                    case 1 => Right(Updated(id, nextVersionumero, diff))
                    case x: Int =>
                      throw new RuntimeException("Unexpected number of updated rows: " + x) // throw exception to cause rollback!
                  }
                }
              case nonOk => DBIO.successful(Left(nonOk))
            }
        }
    }
  }
}