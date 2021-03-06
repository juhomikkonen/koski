package fi.oph.koski.api

import java.time.LocalDate

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.{MockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer.parse
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, MockUsers}
import fi.oph.koski.opiskeluoikeus.ValidationResult
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusValidationSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  implicit val session: KoskiSession = KoskiSession.systemUser
  override def defaultUser = MockUsers.paakayttaja

  "Validoi" - {
    "validi opiskeluoikeus" in {
      val opiskeluoikeusOid = oppija(MockOppijat.eero.oid).tallennettavatOpiskeluoikeudet.flatMap(_.oid).head
      authGet(s"api/opiskeluoikeus/validate/$opiskeluoikeusOid") {
        verifyResponseStatusOk()
        validationResult.errors should be(empty)
      }
    }

    "Päätason suorituksen tyyppi jonka käyttö on estetty" in {
      implicit val accessType = AccessType.read
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = [
              valma
            ]
          }
        """.stripMargin)
      val opiskelija = oppija(MockOppijat.valma.oid)
      mockKoskiValidator(mockConfig).validateAsJson(opiskelija).left.get should equal (KoskiErrorCategory.notImplemented("Päätason suorituksen tyyppi valma ei ole käytössä tässä ympäristössä"))
    }
  }

  private def validationResult = parse[ValidationResult](body)

  private def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.tutkintoRepository,
      KoskiApplicationForTests.koodistoViitePalvelu,
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      KoskiApplicationForTests.ePerusteet,
      config
    )
  }
}
