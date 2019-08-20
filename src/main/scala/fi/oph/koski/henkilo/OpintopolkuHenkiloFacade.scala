package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.elasticsearch.ElasticSearch
import fi.oph.koski.http.Http._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotRepository
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.util.Timing
import org.http4s._

trait OpintopolkuHenkilöFacade {
  def findOppijaByOid(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot]
  def findOppijatNoSlaveOids(oids: List[String]): List[SuppeatOppijaHenkilöTiedot]
  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid]
  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot]
  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot]
  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, SuppeatOppijaHenkilöTiedot]
  def findOppijatByHetusNoSlaveOids(hetus: List[String]): List[SuppeatOppijaHenkilöTiedot]
  def findSlaveOids(masterOid: String): List[Oid]
}

object OpintopolkuHenkilöFacade {
  def apply(config: Config, db: => DB, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): OpintopolkuHenkilöFacade = config.getString("opintopolku.virkailija.url") match {
    case "mock" => new MockOpintopolkuHenkilöFacadeWithDBSupport(db)
    case _ => RemoteOpintopolkuHenkilöFacade(config, perustiedotRepository, elasticSearch)
  }
}

object RemoteOpintopolkuHenkilöFacade {
  def apply(config: Config, perustiedotRepository: => OpiskeluoikeudenPerustiedotRepository, elasticSearch: => ElasticSearch): RemoteOpintopolkuHenkilöFacade = {
    if (config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")) {
      new RemoteOpintopolkuHenkilöFacadeWithMockOids(OppijanumeroRekisteriClient(config), perustiedotRepository, elasticSearch)
    } else {
      new RemoteOpintopolkuHenkilöFacade(OppijanumeroRekisteriClient(config))
    }
  }
}

class RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient) extends OpintopolkuHenkilöFacade with EntityDecoderInstances with Timing {
  def findOppijaByOid(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijaByOid(oid))

  def findOppijatNoSlaveOids(oids: List[Oid]): List[SuppeatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijatNoSlaveOids(oids))

  def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] =
    runTask(oppijanumeroRekisteriClient.findChangedOppijaOids(since, offset, amount))

  def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijaByHetu(hetu))

  def findMasterOppija(oid: String): Option[LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findMasterOppija(oid))

  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findMasterOppijat(oids))

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, SuppeatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOrCreate(createUserInfo))

  def findOppijatByHetusNoSlaveOids(hetus: List[String]): List[SuppeatOppijaHenkilöTiedot] =
    runTask(oppijanumeroRekisteriClient.findOppijatByHetusNoSlaveOids(hetus))

  def findSlaveOids(masterOid: String): List[Oid] = runTask(oppijanumeroRekisteriClient.findSlaveOids(masterOid))
}

class RemoteOpintopolkuHenkilöFacadeWithMockOids(oppijanumeroRekisteriClient: OppijanumeroRekisteriClient, perustiedotRepository: OpiskeluoikeudenPerustiedotRepository, elasticSearch: ElasticSearch) extends RemoteOpintopolkuHenkilöFacade(oppijanumeroRekisteriClient) {
  override def findOppijatNoSlaveOids(oids: List[String]): List[SuppeatOppijaHenkilöTiedot] = {
    val found = super.findOppijatNoSlaveOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap
    oids.map { oid =>
      found.get(oid) match {
        case Some(henkilö) => henkilö
        case None => createMock(oid).toSuppea
      }
    }
  }

  override def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] = {
    val found = super.findMasterOppijat(oids)
    oids.map { oid => oid -> createMockIfNotExists(oid, found) }.toMap
  }

  private def createMockIfNotExists(oid: String, found: Map[String, LaajatOppijaHenkilöTiedot]) = {
    found.get(oid) match {
      case Some(henkilö) => henkilö
      case None => createMock(oid)
    }
  }

  private def createMock(oid: String) = {
    elasticSearch.refreshIndex
    perustiedotRepository.findHenkilöPerustiedotByHenkilöOid(oid).map { henkilö =>
      LaajatOppijaHenkilöTiedot(henkilö.oid, henkilö.sukunimi, henkilö.etunimet, henkilö.kutsumanimi, Some("010101-123N"), None, None, None, None, 0, false)
    }.getOrElse(LaajatOppijaHenkilöTiedot(oid, oid.substring("1.2.246.562.24.".length, oid.length), "Testihenkilö", "Testihenkilö", Some("010101-123N"), None, None, None, None, 0, false))
  }
}

object RemoteOpintopolkuHenkilöFacadeWithMockOids {
  def oppijaWithMockOid(h: TäydellisetHenkilötiedot): LaajatOppijaHenkilöTiedot = {
    LaajatOppijaHenkilöTiedot(
      oid = h.oid,
      sukunimi = h.sukunimi,
      etunimet = h.etunimet,
      kutsumanimi = h.kutsumanimi,
      hetu = h.hetu,
      syntymäaika = h.syntymäaika
    )
  }
}
