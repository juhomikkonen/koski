package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.scalatra._

class RaportointikantaServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with ObservableSupport with ContentEncodingSupport {
  private val service = new RaportointikantaService(application)

  before() {
    noRemoteCalls("/status")
  }

  get("/clear") {
    service.dropAndCreateSchema
    renderObject(Map("ok" -> true))
  }

  get("/load") {
    logger.info("load raportointikanta")
    service.loadRaportointikanta(getBooleanParam("force"))
    renderObject(Map("status" -> "loading"))
  }

  get("/opiskeluoikeudet") {
    streamResponse[LoadResult](service.loadOpiskeluoikeudet(), KoskiSession.systemUser)
  }

  get("/henkilot") {
    renderObject(Map("count" -> service.loadHenkilöt()))
  }

  get("/organisaatiot") {
    renderObject(Map("count" -> service.loadOrganisaatiot()))
  }

  get("/koodistot") {
    renderObject(Map("count" -> service.loadKoodistot()))
  }

  get("/status") {
    renderObject(service.status)
  }

  private def noRemoteCalls(exceptFor: String) =
    if (!request.pathInfo.endsWith(exceptFor) && request.getRemoteHost != "127.0.0.1") {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
}
