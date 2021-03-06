package fi.oph.koski.raportit

import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.raportointikanta.{ROsasuoritusRow, RaportointikantaTestMethods}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Koodistokoodiviite, SisältäväOpiskeluoikeus}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class AmmatillinenTutkintoRaporttiSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsAmmatillinen with BeforeAndAfterAll {

  lazy val repository = AmmatillisenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)

  "Suoritustietojen tarkistusraportti" - {
    lazy val rivi = {
      loadRaportointikantaFixtures
      val rows = testiHenkilöRaporttiRows(defaultRequest)
      rows.length should equal(1)
      rows.head
    }

    "Sisältää oikeat tiedot" in {
      rivi.opiskeluoikeudenAlkamispäivä should equal(Some(date(2012, 9, 1)))
      rivi.koulutusmoduulit should equal("361902")
      rivi.osaamisalat should equal(Some("1590"))
      rivi.tutkintonimikkeet should equal("Ympäristönhoitaja")
      rivi.päätasonSuorituksenNimi should equal("Luonto- ja ympäristöalan perustutkinto")
      rivi.päätasonSuorituksenSuoritusTapa should equal("Ammatillinen perustutkinto")
      rivi.päätasonSuoritustenTilat should equal(Some("Valmis"))
      rivi.viimeisinOpiskeluoikeudenTila should equal(Some("valmistunut"))
      rivi.viimeisinOpiskeluoikeudenTilaAikajaksonLopussa should equal("lasna")
      rivi.opintojenRahoitukset should equal("4")
      rivi.ostettu should equal(false)
      rivi.yksiloity should equal(true)
    }

    "Laskenta" - {
      "Suorituksia yhteesä" in {
        rivi.suoritettujenOpintojenYhteislaajuus should equal(180.0)
      }
      "Ammatilliset tutkinnon osat" - {
        "Valmiiden ammatillisten tutkinnon osien lukumäärä" in {
          rivi.valmiitAmmatillisetTutkinnonOsatLkm should equal(6)
        }
        "Pakolliset ammatilliset tutkinnon osat" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatLkm should equal(6)
        }
        "Valinnaiset ammatilliset tutkinnon osat" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatLkm should equal(0)
        }
        "Näyttöjä ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.näyttöjäAmmatillisessaValmiistaTutkinnonOsistaLkm should equal(3)
        }
        "Tunnustettuja ammatillisissa valmiissa tutkinnon osissa" in {
          rivi.tunnustettujaAmmatillisessaValmiistaTutkinnonOsistaLkm should equal(0)
        }
        "Rahoituksen piirissä tunnustettuja ammatillisia tutkinnon osia" in {
          rivi.rahoituksenPiirissäAmmatillisistaTunnustetuistaTutkinnonOsistaLkm should equal(0)
        }
        "Suoritetut ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.suoritetutAmmatillisetTutkinnonOsatYhteislaajuus should equal(135.0)
        }
        "Pakolliset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.pakollisetAmmatillisetTutkinnonOsatYhteislaajuus should equal(135.0)
        }
        "Valinnaiset ammatilliset tutkinnon osat yhteislaajuus" in {
          rivi.valinnaisetAmmatillisetTutkinnonOsatYhteislaajuus should equal(0)
        }
      }
      "Yhteiset tutkinnon osat" - {
        "Valmiit yhteiset tutkinnon osat lukumäärä" in {
          rivi.valmiitYhteistenTutkinnonOsatLkm should equal(4)
        }
        "Pakollisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.pakollisetYhteistenTutkinnonOsienOsaalueidenLkm should equal(8)
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden lukumäärä" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenLKm should equal(1)
        }
        "Tunnustettuja yhteisten tutkinnon osan osa-alueita valmiista yhteisen tutkinnon osa-alueista" in {
          rivi.tunnustettujaTukinnonOsanOsaalueitaValmiissaTutkinnonOsanOsalueissaLkm should equal(1)
        }
        "Rahoituksen piirissä tutkinnon osan osa-alueita valmiissa yhteisten tutkinnon osan osa-aluiesta" in {
          rivi.rahoituksenPiirissäTutkinnonOsanOsaalueitaValmiissaTutkinnonOsanOsaalueissaLkm should equal(0)
        }
        "Tunnustettuja tutkinnon osia valmiista yhteisen tutkinnon osista" in {
          rivi.tunnustettujaYhteistenTutkinnonOsienValmiistaOsistaLkm should equal(0)
        }
        "Rahoituksen piirissä tunnustetuista yhteisistä tutkinnon osista" in {
          rivi.rahoituksenPiirissäTunnustetuistaYhteisenTutkinnonOsistaLkm should equal(0)
        }
        "Suoritettuja yhteisten tutkinnon osien yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienYhteislaajuus should equal(35.0)
        }
        "Suoritettujen yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.suoritettujenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal(35)
        }
        "Pakollisten yhteisten tutkinnon osioen osa-alueiden yhteislaajuus" in {
          rivi.pakollistenYhteistenTutkinnonOsienOsaalueidenYhteislaajuus should equal(32)
        }
        "Valinnaisten yhteisten tutkinnon osien osa-alueiden yhteislaajuus" in {
          rivi.valinnaistenYhteistenTutkinnonOsienOsaalueidenYhteisLaajuus should equal(3)
        }
      }
      "Valmiit vapaavalintaiset tutkinnon osat lukumäärä" in {
        rivi.valmiitVapaaValintaisetTutkinnonOsatLkm should equal(1)
      }
      "Valmiit tutkintoa yksilöllisesti laajentavat tutkinnon osat lukumäärä" in {
        rivi.valmiitTutkintoaYksilöllisestiLaajentavatTutkinnonOsatLkm should equal(1)
      }
    }
    "Sisällytetyt opiskeluoikeudet" - {
      "Opiskeluoikeuteen sisältyvät opiskeluioikeudet toistesta oppilaitoksesta" in {
        withNewSisällytettyOpiskeluoikeus {
          val aarnenRivit = testiHenkilöRaporttiRows(defaultRequest.copy(oppilaitosOid = MockOrganisaatiot.omnia))
          aarnenRivit.length should equal(2)
          val stadinLinkitettyOpiskeluoikeus = aarnenRivit.find(_.linkitetynOpiskeluoikeudenOppilaitos == "Stadin ammatti- ja aikuisopisto")
          stadinLinkitettyOpiskeluoikeus shouldBe defined
          stadinLinkitettyOpiskeluoikeus.get.suoritettujenOpintojenYhteislaajuus should equal(180.0)
        }
      }
      "Sisältävä opiskeluoikeus ei tule sisällytetyn opiskeluoikeuden oppilaitoksen raportille" in {
        withNewSisällytettyOpiskeluoikeus {
          val rivi = testiHenkilöRaporttiRows(defaultRequest.copy(oppilaitosOid = MockOrganisaatiot.stadinAmmattiopisto))
          rivi.map(_.linkitetynOpiskeluoikeudenOppilaitos) should equal(List(""))
        }
      }
    }
    "Tutkinnon osia voidaan raja arviointipäivän perusteella" - {
      "Tutkinnon osat jotka arvioitu ennen aikaväliä, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(defaultRequest.copy(alku = date(2015, 1, 1), loppu = date(2015, 2, 2), osasuoritustenAikarajaus = true))
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List(40))
      }
      "Tutkinnon osiat jotka arvioitu jälkeen aikavälin, ei oteta mukaan raportille" in {
        val rows = testiHenkilöRaporttiRows(defaultRequest.copy(alku = date(2014, 1, 1), loppu = date(2014, 12, 12), osasuoritustenAikarajaus = true))
        rows.map(_.suoritettujenOpintojenYhteislaajuus) should equal(List(140))
      }
    }

    "Tutkinnon osien erottelu" - {
      val yhteinenTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "400012", tutkinnonOsanRyhmä = None)
      val ammatillinenTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = None)
      val yhteistenTutkinnonOsienOsaalueitaLukioOpintojaTaiMuitaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = yhteisenTutkinnonOsienOsaalueitaTaiLukioTaiMuitaKoodiarvo, tutkinnonOsanRyhmä = None)
      val korkeakouluopintojaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = korkeakouluopintojaKoodiarvo, tutkinnonOsanRyhmä = None)
      val vapaastiValittavaTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = Some("3"))
      val tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow = osasuoritusRow(suorituksenTyyppi = ammatillinenTutkinnonOsa, koulutusmoduulikoodiarvo = "koodiarvo", tutkinnonOsanRyhmä = Some("4"))

      val mahdollisetTutkinnonOsat = List(yhteinenTutkinnonOsaRow, ammatillinenTutkinnonOsaRow, yhteistenTutkinnonOsienOsaalueitaLukioOpintojaTaiMuitaRow, korkeakouluopintojaRow, vapaastiValittavaTutkinnonOsaRow, tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow)

      "Yhteinen tutkinnon osa" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.isYhteinenTutkinnonOsa) should equal(List(
          yhteinenTutkinnonOsaRow
        ))
      }
      "Ammatillinen tutkinnon osa" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.isAmmatillisenTutkinnonOsa) should equal(List(
          ammatillinenTutkinnonOsaRow
        ))
      }
      "Vapaasti valittavat tutkinnon osat" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.tutkinnonOsanRyhmä(_, "3")) should equal(List(
          vapaastiValittavaTutkinnonOsaRow
        ))
      }
      "Tutkintoa yksilöllisesti laajentavat tutkinnon osat" in {
        mahdollisetTutkinnonOsat.filter(AmmatillinenRaporttiUtils.tutkinnonOsanRyhmä(_, "4")) should equal(List(
          tutkintoaYksilöllisestiLaajentavaTutkinnonOsaRow
        ))
      }
    }

    "raportin lataaminen toimii (ja tuottaa audit log viestin)" in {
      verifyRaportinLataaminen(apiUrl = "api/raportit/ammatillinentutkintosuoritustietojentarkistus", expectedRaporttiNimi = AmmatillinenTutkintoSuoritustietojenTarkistus.toString, expectedFileNamePrefix = "suoritustiedot")
    }
  }

  override def beforeAll(): Unit = loadRaportointikantaFixtures

  private val defaultHetu = MockOppijat.ammattilainen.hetu.get

  private val defaultRequest = AikajaksoRaporttiAikarajauksellaRequest(
    oppilaitosOid = MockOrganisaatiot.stadinAmmattiopisto,
    alku =  date(2016, 1, 1),
    loppu = date(2016,5 , 30),
    osasuoritustenAikarajaus = false,
    downloadToken = None,
    password = ""
  )

  private def testiHenkilöRaporttiRows(request: AikajaksoRaporttiAikarajauksellaRequest): Seq[SuoritustiedotTarkistusRow] =
    AmmatillinenTutkintoRaportti.buildRaportti(request, repository).filter(_.hetu.contains(defaultHetu)).toList

  private def withNewSisällytettyOpiskeluoikeus(f: => Unit) = {
    resetFixtures
    val omnia = MockOrganisaatioRepository.findByOppilaitosnumero("10054").get
    val omnianOpiskeluoikeus = makeOpiskeluoikeus(date(2016, 1, 1), omnia, omnia.oid)
    val oppija = MockOppijat.ammattilainen

    putOpiskeluoikeus(omnianOpiskeluoikeus, oppija){}

    val stadinOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).find(_.oppilaitos.map(_.oid).contains(MockOrganisaatiot.stadinAmmattiopisto)).map{case oo: AmmatillinenOpiskeluoikeus => oo}.get
    val omnianOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ammattilainen.oid).oid.get

    putOpiskeluoikeus(sisällytäOpiskeluoikeus(stadinOpiskeluoikeus, SisältäväOpiskeluoikeus(omnia, omnianOpiskeluoikeusOid)), oppija){}
    loadRaportointikantaFixtures
    (f)
  }

  private def osasuoritusRow(suorituksenTyyppi: String, koulutusmoduulikoodiarvo: String, tutkinnonOsanRyhmä: Option[String]) = {
    ROsasuoritusRow(
      osasuoritusId = 1L,
      ylempiOsasuoritusId = None,
      päätasonSuoritusId = 1L,
      opiskeluoikeusOid = "1",
      suorituksenTyyppi = suorituksenTyyppi,
      koulutusmoduuliKoodiarvo = koulutusmoduulikoodiarvo,
      koulutusmoduuliPaikallinen = false,
      koulutusmoduuliKoodisto = None,
      koulutusmoduuliLaajuusArvo = None,
      koulutusmoduuliLaajuusYksikkö = None,
      koulutusmoduuliPakollinen = None,
      koulutusmoduuliNimi = None,
      koulutusmoduuliOppimääräNimi = None,
      koulutusmoduuliKieliaineNimi = None,
      vahvistusPäivä = None,
      arviointiArvosanaKoodiarvo = None,
      arviointiArvosanaKoodisto = None,
      arviointiHyväksytty = None,
      arviointiPäivä = None,
      näytönArviointiPäivä = None,
      tunnustettu = false,
      data = mockJValueData(tutkinnonOsanRyhmä)
    )
  }

  private def mockJValueData(tutkinnonOsanRyhmäKoodiarvo: Option[String]) = {
    implicit val user = SensitiveDataAllowed.SystemUser
    JsonSerializer.serialize(Map(
      "tutkinnonOsanRyhmä" -> tutkinnonOsanRyhmäKoodiarvo.map(Koodistokoodiviite(_, "ammatillisentutkinnonosanryhma"))
    ))
  }

  private lazy val ammatillinenTutkinnonOsa = "ammatillisentutkinnonosa"
  private lazy val yhteisenTutkinnonOsienOsaalueitaTaiLukioTaiMuitaKoodiarvo = "1"
  private lazy val korkeakouluopintojaKoodiarvo = "2"
}
