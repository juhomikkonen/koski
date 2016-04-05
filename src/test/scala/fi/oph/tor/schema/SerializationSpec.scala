package fi.oph.tor.schema

import fi.oph.tor.documentation.{AmmatillinenExampleData, Examples}
import fi.oph.tor.json.Json
import fi.oph.tor.log.Logging
import org.scalatest.{FunSpec, Matchers}

class SerializationSpec extends FunSpec with Matchers with Logging {
  describe("Serialization / deserialization") {
    it("Hyväksiluku") {
      val jsonString = Json.write(AmmatillinenExampleData.hyväksiluku)
      val hyväksiluku = Json.read[Hyväksiluku](jsonString)
      hyväksiluku should(equal(AmmatillinenExampleData.hyväksiluku))
    }

    describe("Examples") {
      Examples.examples.foreach { example =>
        it(example.name) {
          val jsonString = Json.write(example.data)
          val oppija = Json.read[TorOppija](jsonString)
          oppija should(equal(example.data))
          logger.info(example.name + " ok")
        }
      }
    }
  }
}
