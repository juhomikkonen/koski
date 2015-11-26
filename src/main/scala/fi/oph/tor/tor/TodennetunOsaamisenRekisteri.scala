package fi.oph.tor.tor

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.schema._
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.user.UserContext

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opiskeluOikeusRepository: OpiskeluOikeusRepository,
                                   tutkintoRepository: TutkintoRepository,
                                   oppilaitosRepository: OppilaitosRepository,
                                   arviointiAsteikot: ArviointiasteikkoRepository) {

  def findOppijat(query: String)(implicit userContext: UserContext): Seq[FullHenkilö] = {
    val oppijat: List[FullHenkilö] = oppijaRepository.findOppijat(query)
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered
  }

  def createOrUpdate(oppija: TorOppija)(implicit userContext: UserContext): Either[HttpStatus, Henkilö.Id] = {
    if (oppija.opiskeluoikeudet.length == 0) {
      Left(HttpStatus.badRequest("At least one OpiskeluOikeus required"))
    }
    else {
      HttpStatus.fold(oppija.opiskeluoikeudet.map(x => HttpStatus.ok)) match {
        case error if error.isError => Left(error)
        case _ =>


          val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilö match {
            case h:NewHenkilö => oppijaRepository.findOrCreate(oppija).right.map(VerifiedOppijaOid(_))
            case h:HenkilöWithOid => Right(UnverifiedOppijaOid(h.oid, oppijaRepository))
          }
          oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedOppijaOid =>
            val opiskeluOikeusCreationResults = oppija.opiskeluoikeudet.map { opiskeluOikeus =>
              opiskeluOikeusRepository.createOrUpdate(oppijaOid, opiskeluOikeus)
            }
            opiskeluOikeusCreationResults.find(_.isLeft) match {
              case Some(Left(error)) => Left(error)
              case _ => Right(oppijaOid.oppijaOid)
            }
          }
      }
    }
  }

  /*

  def validateOpiskeluOikeus(opiskeluOikeus: OpiskeluOikeus)(implicit userContext: UserContext): HttpStatus = {
    tutkintoRepository.findPerusteRakenne(opiskeluOikeus.tutkinto.ePerusteetDiaarinumero)(arviointiAsteikot) match {
      case None =>
        HttpStatus.badRequest("Invalid ePeruste: " + opiskeluOikeus.tutkinto.ePerusteetDiaarinumero)
      case Some(rakenne) =>
        HttpStatus.ifThen(!userContext.hasReadAccess(opiskeluOikeus.oppilaitosOrganisaatio)) { HttpStatus.forbidden("Forbidden") }
          .ifOkThen{ HttpStatus
              .each(opiskeluOikeus.suoritustapa.filter(!Suoritustapa.apply(_).isDefined)) { suoritustapa => HttpStatus.badRequest("Invalid suoritustapa: " + suoritustapa)}
              .appendEach(opiskeluOikeus.osaamisala.filter(osaamisala => !TutkintoRakenne.findOsaamisala(rakenne, osaamisala).isDefined)) { osaamisala => HttpStatus.badRequest("Invalid osaamisala: " + osaamisala) }
              .appendEach(opiskeluOikeus.suoritukset)(validateSuoritus(_, opiskeluOikeus.suoritustapa.flatMap(Suoritustapa.apply), rakenne))
          }
    }
  }

  def validateSuoritus(suoritus: Suoritus, suoritusTapa: Option[Suoritustapa], rakenne: TutkintoRakenne): HttpStatus = {
    suoritusTapa match {
      case None => HttpStatus.badRequest("Suoritustapa puuttuu")
      case Some(suoritusTapa) => TutkintoRakenne.findTutkinnonOsa(rakenne, suoritusTapa, suoritus.koulutusModuuli) match {
        case None =>
          HttpStatus.badRequest("Tuntematon tutkinnon osa: " + suoritus.koulutusModuuli)
        case Some(tutkinnonOsa) =>
          HttpStatus.each(suoritus.arviointi) { arviointi =>
            HttpStatus
              .ifThen(Some(arviointi.asteikko) != tutkinnonOsa.arviointiAsteikko) {
              HttpStatus.badRequest("Perusteiden vastainen arviointiasteikko: " + arviointi.asteikko)
            }
              .ifOkThen {
              rakenne.arviointiAsteikot.find(_.koodisto == arviointi.asteikko) match {
                case Some(asteikko) if (!asteikko.arvosanat.contains(arviointi.arvosana)) =>
                  HttpStatus.badRequest("Arvosana " + Json.write(arviointi.arvosana) + " ei kuulu asteikkoon " + Json.write(asteikko))
                case None =>
                  HttpStatus.internalError("Asteikkoa " + arviointi.asteikko + " ei löydy tutkintorakenteesta")
                case _ =>
                  HttpStatus.ok
              }
            }
          }
      }
    }
  }

  */

  def userView(oid: String)(implicit userContext: UserContext): Either[HttpStatus, TorOppija] = {
    oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluoikeudetForOppija(oppija) match {
          case Nil => notFound(oid)
          case opiskeluoikeudet => Right(TorOppija(oppija, opiskeluoikeudet))
        }
      case None => notFound(oid)
    }
  }

  def notFound(oid: String): Left[HttpStatus, Nothing] = {
    Left(HttpStatus.notFound(s"Oppija with oid: $oid not found"))
  }

  // TODO: perusteen rakenne haettava erikseen

  //tutkinto   <- tutkintoRepository.findByEPerusteDiaarinumero(opiskeluOikeus.suoritus.koulutusmoduulitoteutus.asInstanceOf[TutkintoKoulutustoteutus].koulutusmoduuli.perusteenDiaarinumero.get) // <- TODO: nasty
  //tutkinto = tutkinto.copy(rakenne = tutkintoRepository.findPerusteRakenne(tutkinto.ePerusteetDiaarinumero)(arviointiAsteikot)),

  private def opiskeluoikeudetForOppija(oppija: FullHenkilö)(implicit userContext: UserContext): Seq[OpiskeluOikeus] = {
    for {
      opiskeluOikeus   <- opiskeluOikeusRepository.findByOppijaOid(oppija.oid)
      oppilaitos <- oppilaitosRepository.findById(opiskeluOikeus.oppilaitos.oid)
    } yield {
      opiskeluOikeus.copy(
        oppilaitos = oppilaitos
      )
    }
  }
}

