package fi.oph.koski.perftest

import java.util.UUID

import fi.oph.koski.documentation.{AmmatillinenExampleData}
import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.perftest.RandomName.{randomFirstName, randomLastName}
import fi.oph.koski.schema._
import org.json4s._
import java.io._

import scala.util.Random

/**
  * eHoks palvelun suorituskykytestausta varten opiskeluoikeuksien luonti tiedostoon
  */
object AmmatillinenOpiskeluoikeusInserter extends App {
  AmmatillinenOpiskeluoikeusInserterScenario.operation()
}

object AmmatillinenOpiskeluoikeusInserterScenario extends KoskidevHttpSpecification with Logging {
  implicit val formats = DefaultFormats
  def lähdejärjestelmät = List("primus", "winnova", "helmi", "winha", "peppi", "studentaplus", "rediteq")
  def lähdejärjestelmäId = Some(LähdejärjestelmäId(Some(UUID.randomUUID().toString), Koodistokoodiviite(lähdejärjestelmät(Random.nextInt(lähdejärjestelmät.length)), "lahdejarjestelma")))

  lazy val outputpath = sys.env.getOrElse("outputpath", "~/temp/opiskeluoikeudet.txt")

  val hetu = new RandomHetu()

  override def logger = super.logger

  def operation() = {

    val pw = new PrintWriter(new File(outputpath))

    pw.write("[")
    for(a <- 1 to 1000) {
      val ammattikoulu = Oppilaitos("1.2.246.562.10.28646781493", None, None)
      val ammattikouluOpiskeluoikeus = AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(ammattikoulu, ammattikoulu.toOidOrganisaatio).copy(lähdejärjestelmänId = lähdejärjestelmäId)

      val kutsumanimi = randomFirstName
      val henkilö: UusiHenkilö = UusiHenkilö(hetu.nextHetu, kutsumanimi + " " + randomFirstName, Some(kutsumanimi), randomLastName)

      val oppija: Oppija = Oppija(henkilö, List(ammattikouluOpiskeluoikeus))
      val body = JsonSerializer.writeWithRoot(oppija).getBytes("utf-8")

      val cookieHeaders = Map("Cookie" -> s"SERVERID=koski-app1")
      val contentTypeHeaders = jsonContent
      val headers: Iterable[(String, String)] = cookieHeaders ++ contentTypeHeaders ++ authHeaders()

      submit("PUT", "api/oppija", Seq.empty, headers, body) {

        println("put resp:" + response.body)

        if(a != 1) {
          pw.write(",\n")
        }
        pw.write(response.body)

      }

    }
    pw.write("]")



    pw.close

  }


}

