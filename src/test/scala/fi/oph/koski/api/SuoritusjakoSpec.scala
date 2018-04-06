package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.suoritusjako.{SuoritusIdentifier, SuoritusjakoResponse}
import org.scalatest.FreeSpec

class SuoritusjakoSpec extends FreeSpec with SuoritusjakoTestMethods {
  var secretYksiSuoritus: Option[String] = None
  var secretKaksiSuoritusta: Option[String] = None

  "Suoritusjaon lisääminen" - {
    "onnistuu" - {
      "yhdellä oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }]"""

        putSuoritusjako(json){
          verifyResponseStatusOk()
          secretYksiSuoritus = Option(JsonSerializer.parse[SuoritusjakoResponse](response.body).secret)
        }
      }

      "kahdella oikeellisella suorituksella" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "6"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatusOk()
          secretKaksiSuoritusta = Option(JsonSerializer.parse[SuoritusjakoResponse](response.body).secret)
        }
      }
    }

    "epäonnistuu" - {
      "yhdellä suorituksella, jota ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "kahdella suorituksella, joista toista ei löydy" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7"
        }, {
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "9"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(404, KoskiErrorCategory.notFound.suoritustaEiLöydy())
        }
      }

      "puuttellisilla tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "ylimääräisillä tiedoilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7",
          "extra": "extra"
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }

      "epäkelvolla JSON-dokumentilla" in {
        val json =
          """[{
          "oppilaitosOid": "1.2.246.562.10.64353470871",
          "suorituksenTyyppi": "perusopetuksenvuosiluokka",
          "koulutusmoduulinTunniste": "7
        }]"""

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.json("Epäkelpo JSON-dokumentti"))
        }
      }

      "tyhjällä suorituslistalla" in {
        val json = "[]"

        put("api/suoritusjako", body = json, headers = kansalainenLoginHeaders("180497-112F") ++ jsonContent){
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format())
        }
      }
    }
  }

  "Suoritusjaon hakeminen" - {
    "onnistuu" - {
      "yhden jaetun suorituksen salaisuudella" in {
        getSuoritusjako(secretYksiSuoritus.get){
          verifyResponseStatusOk()

          verifySuoritusIds(List(SuoritusIdentifier(
            oppilaitosOid = "1.2.246.562.10.64353470871",
            suorituksenTyyppi = "perusopetuksenvuosiluokka",
            koulutusmoduulinTunniste = "7"
          )))
        }
      }

      "kahden jaetun suorituksen salaisuudella" in {
        getSuoritusjako(secretKaksiSuoritusta.get){
          verifyResponseStatusOk()

          verifySuoritusIds(List(
            SuoritusIdentifier(
              oppilaitosOid = "1.2.246.562.10.64353470871",
              suorituksenTyyppi = "perusopetuksenvuosiluokka",
              koulutusmoduulinTunniste = "7"
            ),
            SuoritusIdentifier(
              oppilaitosOid = "1.2.246.562.10.64353470871",
              suorituksenTyyppi = "perusopetuksenvuosiluokka",
              koulutusmoduulinTunniste = "6"
            )
          ))
        }
      }
    }

    "epäonnistuu" - {
      "epäkelvolla salaisuudella" in {
        getSuoritusjako("2.2.246.562.10.64353470871"){
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }
    }
  }
}
