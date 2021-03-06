package fi.oph.koski.api

import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{KoskiSession, UserWithPassword}
import fi.oph.koski.schema._
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s._
import org.json4s.jackson.JsonMethods

import scala.language.implicitConversions
import scala.reflect.runtime.universe.TypeTag

trait PutOpiskeluoikeusTestMethods[Oikeus <: Opiskeluoikeus] extends OpiskeluoikeusTestMethods with OpiskeluoikeusData[Oikeus] {
  def tag: TypeTag[Oikeus]

  val koodisto: KoodistoViitePalvelu = MockKoodistoViitePalvelu
  val oppijaPath = "/api/oppija"

  implicit def any2j[T : TypeTag](o: T): JValue = JsonSerializer.serializeWithUser(KoskiSession.systemUser)(o)

  implicit def oppijaHenkilöToHenkilöJaOid(o: OppijaHenkilö): HenkilötiedotJaOid = o.toHenkilötiedotJaOid

  def putOpiskeluoikeus[A](opiskeluoikeus: Opiskeluoikeus, henkilö: Henkilö = defaultHenkilö, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    putOppija(makeOppija(henkilö, List(opiskeluoikeus)), headers)(f)
  }

  def putHenkilö[A](henkilö: Henkilö)(f: => A): Unit = {
    import fi.oph.koski.schema.KoskiSchema.deserializationContext
    putOppija(JsonSerializer.serializeWithRoot(SchemaValidatingExtractor.extract[Oppija](makeOppija(opiskeluOikeudet = List(defaultOpiskeluoikeus))(tag)).right.get.copy(henkilö = henkilö)))(f)
  }

  def putOppija[A](oppija: JValue, headers: Headers = authHeaders() ++ jsonContent)(f: => A): A = {
    val jsonString = JsonMethods.pretty(oppija)
    val result = put("api/oppija", body = jsonString, headers = headers)(f)
    refreshElasticSearchIndexes
    result
  }

  def request[A](path: String, contentType: String, content: String, method: String)(f: => A): Unit = {
    submit(method, path, body = content.getBytes("UTF-8"), headers = authHeaders() ++ jsonContent) (f)
  }

  def createOrUpdate(oppija: Henkilö, opiskeluoikeus: Opiskeluoikeus, check: => Unit = { verifyResponseStatusOk() }, user: UserWithPassword = defaultUser) = {
    putOppija(JsonSerializer.serializeWithRoot(Oppija(oppija, List(opiskeluoikeus))), headers = authHeaders(user) ++ jsonContent){
      check
      lastOpiskeluoikeusByHetu(oppija)
    }
  }

  def createOpiskeluoikeus[T <: Opiskeluoikeus](oppija: Henkilö, opiskeluoikeus: T, resetFixtures: Boolean = false, user: UserWithPassword = defaultUser): T = {
    if (resetFixtures) this.resetFixtures
    createOrUpdate(oppija, opiskeluoikeus, user = user).asInstanceOf[T]
  }

  def makeOppija[T: TypeTag](henkilö: Henkilö = defaultHenkilö, opiskeluOikeudet: List[T]): JValue = JObject(
    "henkilö" -> JsonSerializer.serializeWithRoot(henkilö),
    "opiskeluoikeudet" -> JsonSerializer.serializeWithRoot(opiskeluOikeudet)
  )

  import fi.oph.koski.schema.KoskiSchema.deserializationContext
  def readPutOppijaResponse: PutOppijaResponse = {
    SchemaValidatingExtractor.extract[PutOppijaResponse](JsonMethods.parse(body)).right.get
  }
}

case class PutOppijaResponse(henkilö: ResponseHenkilö, opiskeluoikeudet: List[ResponseOpiskeluoikeus])
case class ResponseHenkilö(oid: String)
case class ResponseOpiskeluoikeus(oid: String, versionumero: Int)
