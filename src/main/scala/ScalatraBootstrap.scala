import fi.oph.koski.cache.CacheServlet
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db._
import fi.oph.koski.documentation.{DocumentationApiServlet, DocumentationServlet, KoodistoServlet}
import fi.oph.koski.editor.{EditorKooditServlet, EditorServlet}
import fi.oph.koski.etk.ElaketurvakeskusServlet
import fi.oph.koski.fixture.FixtureServlet
import fi.oph.koski.healthcheck.{HealthCheckApiServlet, HealthCheckHtmlServlet}
import fi.oph.koski.henkilo.HenkilötiedotServlet
import fi.oph.koski.history.KoskiHistoryServlet
import fi.oph.koski.kela.KelaServlet
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizationServlet
import fi.oph.koski.log.Logging
import fi.oph.koski.luovutuspalvelu.{LuovutuspalveluServlet, PalveluvaylaServlet, TilastokeskusServlet}
import fi.oph.koski.mydata.{ApiProxyServlet, MyDataReactServlet, MyDataServlet}
import fi.oph.koski.omattiedot.{OmatTiedotHtmlServlet, OmatTiedotServlet}
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusServlet, OpiskeluoikeusValidationServlet}
import fi.oph.koski.oppija.{OppijaServlet, OppijaServletV2}
import fi.oph.koski.oppilaitos.OppilaitosServlet
import fi.oph.koski.organisaatio.OrganisaatioServlet
import fi.oph.koski.permission.PermissionCheckServlet
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotServlet
import fi.oph.koski.preferences.PreferencesServlet
import fi.oph.koski.pulssi.{PulssiHtmlServlet, PulssiServlet}
import fi.oph.koski.raportit.RaportitServlet
import fi.oph.koski.raportointikanta.RaportointikantaServlet
import fi.oph.koski.servlet._
import fi.oph.koski.sso.{CasServlet, LocalLoginServlet, SSOConfig, ShibbolethLoginServlet}
import fi.oph.koski.suoritusjako.{SuoritusjakoServlet, SuoritusjakoServletV2}
import fi.oph.koski.suoritusote.SuoritusServlet
import fi.oph.koski.sure.SureServlet
import fi.oph.koski.tiedonsiirto.TiedonsiirtoServlet
import fi.oph.koski.todistus.TodistusServlet
import fi.oph.koski.tutkinto.TutkinnonPerusteetServlet
import fi.oph.koski.util.Futures
import fi.oph.koski.valvira.ValviraServlet
import fi.oph.koski.ytr.{YtrKoesuoritusApiServlet, YtrKoesuoritusServlet}
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging with GlobalExecutionContext {
  override def init(context: ServletContext) = try {
    def mount(path: String, handler: Handler) = context.mount(handler, path)

    implicit val application = Option(context.getAttribute("koski.application").asInstanceOf[KoskiApplication]).getOrElse(KoskiApplication.apply)

    application.init // start parallel initialization tasks

    mount("/", new IndexServlet)
    mount("/omattiedot", new OmatTiedotHtmlServlet)
    mount("/login", new VirkailijaLoginPageServlet)
    mount("/login/shibboleth", new OppijaLoginPageServlet)
    mount("/pulssi", new PulssiHtmlServlet)
    mount("/todistus", new TodistusServlet)
    mount("/opintosuoritusote", new SuoritusServlet)
    mount("/documentation", new RedirectServlet("/dokumentaatio", true))
    mount("/dokumentaatio", new DocumentationServlet)
    mount("/eisuorituksia", new EiSuorituksiaServlet)
    mount("/opinnot", new SuoritusjakoHtmlServlet)
    mount("/virhesivu", new VirhesivuServlet)
    mount("/api/documentation", new DocumentationApiServlet)
    mount("/api/editor", new EditorServlet)
    mount("/api/editor/koodit", new EditorKooditServlet)
    mount("/api/elaketurvakeskus", new ElaketurvakeskusServlet)
    mount("/api/healthcheck", new HealthCheckApiServlet)
    mount("/api/henkilo", new HenkilötiedotServlet)
    mount("/api/koodisto", new KoodistoServlet)
    mount("/api/omattiedot", new OmatTiedotServlet)
    mount("/api/suoritusjako", new SuoritusjakoServlet)
    mount("/api/suoritusjakoV2", new SuoritusjakoServletV2)
    mount("/api/opiskeluoikeus", new OpiskeluoikeusServlet)
    mount("/api/opiskeluoikeus/perustiedot", new OpiskeluoikeudenPerustiedotServlet)
    mount("/api/opiskeluoikeus/validate", new OpiskeluoikeusValidationServlet)
    mount("/api/opiskeluoikeus/historia", new KoskiHistoryServlet)
    mount("/api/oppija", new OppijaServlet)
    mount("/api/v2/oppija", new OppijaServletV2)
    mount("/api/oppilaitos", new OppilaitosServlet)
    mount("/api/organisaatio", new OrganisaatioServlet)
    mount("/api/permission", new PermissionCheckServlet)
    mount("/api/pulssi", new PulssiServlet)
    mount("/api/preferences", new PreferencesServlet)
    mount("/api/tiedonsiirrot", new TiedonsiirtoServlet)
    mount("/api/tutkinnonperusteet", new TutkinnonPerusteetServlet)
    mount("/api/localization", new LocalizationServlet)
    mount("/api/raportit", new RaportitServlet)
    mount("/api/raportointikanta", new RaportointikantaServlet)
    mount("/api/sure", new SureServlet)
    mount("/api/luovutuspalvelu", new LuovutuspalveluServlet)
    mount("/api/luovutuspalvelu/valvira", new ValviraServlet)
    mount("/api/luovutuspalvelu/kela", new KelaServlet)
    mount("/api/palveluvayla", new PalveluvaylaServlet)
    mount("/api/luovutuspalvelu/haku", new TilastokeskusServlet)
    mount("/api/omadata/oppija", new ApiProxyServlet)
    mount("/api/omadata", new MyDataServlet)
    mount("/api/ytrkoesuoritukset", new YtrKoesuoritusApiServlet)
    mount("/omadata", new MyDataReactServlet)
    mount("/koesuoritus", new YtrKoesuoritusServlet)
    mount("/healthcheck", new HealthCheckHtmlServlet)
    mount("/user", new UserServlet)
    if (!SSOConfig(application.config).isCasSsoUsed) {
      mount("/user/login", new LocalLoginServlet)
    }
    mount("/user/logout", new LogoutServlet)
    mount("/user/redirect", new LogoutRedirectServlet)
    if (application.features.shibboleth) {
      mount("/user/shibbolethlogin", ShibbolethLoginServlet(application))
    }
    mount("/cas", new CasServlet)
    mount("/cache", new CacheServlet)

    Futures.await(application.init) // await for all initialization tasks to complete

    if (application.fixtureCreator.shouldUseFixtures) {
      context.mount(new FixtureServlet, "/fixtures")
      application.fixtureCreator.resetFixtures
    }
  } catch {
    case e: Exception =>
      logger.error(e)("Server startup failed: " + e.getMessage)
      System.exit(1)
  }

  override def destroy(context: ServletContext) = {
  }
}
