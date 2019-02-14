package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.AmmatillinenExampleData
import org.scalatest.{FreeSpec, Matchers}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods

class ElaketurvakeskusSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsAmmatillinen {

  "Tutkintotietojen muodostaminen" - {
    "Valmiit ammatilliset perustutkinnot" in {
      resetFixtures
      loadRaportointikantaFixtures

      val mockOppija = MockOppijat.ammattilainen
      val mockOppijanOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(mockOppija.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

      postAikajakso(date(2016, 1, 1), date(2016, 12, 12), 2016) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[EtkResponse](body)
        response.vuosi should equal(2016)
        response.tutkintojenLkm should equal(1)
        response.aikaleima shouldBe a[Timestamp]
        response.tutkinnot should equal(List(
          EtkTutkintotieto(
            EtkHenkilö(mockOppija.hetu, date(1918, 6, 28), mockOppija.sukunimi, mockOppija.etunimet),
            EtkTutkinto("ammatillinenkoulutus", date(2012, 9, 1), Some(date(2016, 5, 31))),
            Some(EtkViite(mockOppijanOpiskeluoikeusOid, 1, mockOppija.oid)))
        ))
      }
    }
    "Oppija jolla monta oidia" in {
      val slaveMock = MockOppijat.slave.henkilö
      val masterMock = MockOppijat.master

      createOrUpdate(slaveMock, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis())
      loadRaportointikantaFixtures

      val slaveOppijanOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(slaveMock.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

      postAikajakso(date(2016, 1, 1), date(2016, 12, 31), 2016) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EtkResponse](body).tutkinnot should contain(
          EtkTutkintotieto(
            EtkHenkilö(masterMock.hetu, date(1997, 10, 10), masterMock.sukunimi, masterMock.etunimet),
            EtkTutkinto("ammatillinenkoulutus", date(2012, 9, 1), Some(date(2016, 5, 31))),
            Some(EtkViite(slaveOppijanOpiskeluoikeusOid, 1, slaveMock.oid)))
        )
      }
    }
  }

  private def postAikajakso[A](alku: LocalDate, loppu: LocalDate, vuosi: Int)(f: => A): A = {
    post(
      "api/eläketurvakeskus/ammatillisetperustutkinnot",
      JsonSerializer.writeWithRoot(EtkTutkintotietoRequest(alku, loppu, vuosi)),
      headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent
    )(f)
  }
}