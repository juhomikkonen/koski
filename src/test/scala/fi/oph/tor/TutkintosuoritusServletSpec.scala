package fi.oph.tor

import fi.oph.tor.fixture.TutkintoSuoritusTestData
import TutkintoSuoritusTestData.tutkintosuoritus1
import fi.oph.tor.json.Json
import fi.oph.tor.model.{Identified, Tutkintosuoritus}
import org.scalatest.FunSuiteLike
import org.scalatra.test.scalatest.ScalatraSuite

class TutkintosuoritusServletSpec extends ScalatraSuite with FunSuiteLike with TorTest {
  lazy val tor: TodennetunOsaamisenRekisteri = initLocalRekisteri
  lazy val tutkintosuoritusServlet = new TutkintosuoritusServlet(tor)
  addServlet(tutkintosuoritusServlet, "/*")

  test("roundtrip") {
    post("/", Json.write(tutkintosuoritus1), Map("Content-type" -> "application/json")) {
      status should equal (200)
    }

    get("/") {
      status should equal (200)
      response.getContentType() should equal ("application/json;charset=utf-8")
      val suoritukset: List[Tutkintosuoritus] = Json.read[List[Tutkintosuoritus]](body)
      suoritukset.map(Identified.withoutId) should contain (tutkintosuoritus1)
      suoritukset.map(_.id) should not contain None
    }
  }
}
