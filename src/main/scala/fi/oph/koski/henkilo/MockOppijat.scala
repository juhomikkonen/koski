package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.henkilo
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  private val oppijat = new MockOppijat

  // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia, eikä tätä lisätä henkilöpalveluun.
  val tyhjä = UusiHenkilö("230872-7258", "Tero", Some("Tero"), "Tyhjä")

  val hetuton = oppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999123", sukunimi = "Hetuton", etunimet = "Heikki", kutsumanimi = "Heikki", hetu = None, syntymäaika = Some(LocalDate.of(1977, 2, 24)), yksilöity = false))
  val syntymäajallinen = oppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999124", sukunimi = "Syntynyt", etunimet = "Sylvi", kutsumanimi = "Sylvi", hetu = Some("220627-833V"), syntymäaika = Some(LocalDate.of(1970, 1, 1))))
  val eero = oppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = oppijat.oppija("Eerola", "Jouni", "081165-793C")
  val markkanen = oppijat.oppija("Markkanen-Fagerström", "Eéro Jorma-Petteri", "080154-770R")
  val teija = oppijat.oppija("Tekijä", "Teija", "251019-039B")
  val tero = oppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "280608-6619")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = oppijat.oppija("Koululainen", "Kaisa", "220109-784L")
  val luokallejäänyt = oppijat.oppija("Luokallejäänyt", "Lasse", "170186-6520")
  val ysiluokkalainen = oppijat.oppija("Ysiluokkalainen", "Ylermi", "160932-311V")
  val vuosiluokkalainen = oppijat.oppija("Vuosiluokkalainen", "Ville", "010100-325X")
  val monessaKoulussaOllut = oppijat.oppija("Monikoululainen", "Miia", "180497-112F")
  val lukiolainen = oppijat.oppija("Lukiolainen", "Liisa", "020655-2479")
  val lukioKesken = oppijat.oppija("Lukiokesken", "Leila", "190363-279X")
  val uusiLukio = oppijat.oppija("Uusilukio", "Ulla", "250605A518Y")
  val uusiLukionAineopiskelija = oppijat.oppija("Uusilukionaineopiskelija", "Urho", "010705A6119")
  val lukionAineopiskelija = oppijat.oppija("Lukioaineopiskelija", "Aino", "210163-2367")
  val lukionAineopiskelijaAktiivinen = oppijat.oppija("Lukioaineopiskelija", "Aktiivinen", "200300-624E")
  val lukionEiTiedossaAineopiskelija = oppijat.oppija("Erkki", "Eitiedossa", "151132-746V")
  val ammattilainen = oppijat.oppija("Ammattilainen", "Aarne", "280618-402H")
  val tutkinnonOsaaPienempiKokonaisuus = oppijat.oppija("Pieni-Kokonaisuus", "Pentti", "040754-054W")
  val muuAmmatillinen = oppijat.oppija("Muu-Ammatillinen", "Marjo", "130320-899Y")
  val muuAmmatillinenKokonaisuuksilla = oppijat.oppija("Kokonaisuuksilla", "Keijo", "130174-452V")
  val ammatilliseenTetäväänValmistavaMuuAmmatillinen = oppijat.oppija("Tehtävään-Valmistava", "Tauno", "050192-777U")
  val erkkiEiperusteissa = oppijat.oppija("Eiperusteissa", "Erkki", "201137-361Y")
  val amis = oppijat.oppija("Amis", "Antti", "211097-402L")
  val liiketalous = oppijat.oppija("Liiketalous", "Lilli", "160525-780Y")
  val dippainssi = oppijat.oppija("Dippainssi", "Dilbert", "100869-192W")
  val korkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kikka", "150113-4146")
  val amkValmistunut = oppijat.oppija("Amis", "Valmis", "250686-102E", vanhaHetu = Some("250686-6493"))
  val opintojaksotSekaisin = oppijat.oppija("Hassusti", "Opintojaksot", "090992-3237")
  val amkKesken = oppijat.oppija("Amiskesken", "Jalmari", "090197-411W")
  val amkKeskeytynyt = oppijat.oppija("Pudokas", "Valtteri", "170691-3962")
  val monimutkainenKorkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kompleksi", "060458-331R")
  val virtaEiVastaa = oppijat.oppija("Virtanen", "Eivastaa", "250390-680P")
  val oppiaineenKorottaja = oppijat.oppija("Oppiaineenkorottaja", "Olli", "110738-839L")
  val montaOppiaineenOppimäärääOpiskeluoikeudessa = oppijat.oppija("Mervi", "Monioppiaineinen", "131298-5248")
  val virtaKaksiPäätösonSuoritusta = oppijat.oppija("Kaksi-Päinen", "Ville", "270680-459P")
  val aikuisOpiskelija = oppijat.oppija("Aikuisopiskelija", "Aini", "280598-2415", vanhaHetu = Some("280598-326W"))
  val kymppiluokkalainen = oppijat.oppija("Kymppiluokkalainen", "Kaisa", "131025-6573", sukupuoli = Some("2"), kotikunta = Some("Kontu"))
  val luva = oppijat.oppija("Lukioonvalmistautuja", "Luke", "211007-442N")
  val valma = oppijat.oppija("Amikseenvalmistautuja", "Anneli", "130404-054C")
  val ylioppilas = oppijat.oppija("Ylioppilas", "Ynjevi", "210244-374K", vanhaHetu = Some("210244-073V"))
  val ylioppilasLukiolainen = oppijat.oppija("Ylioppilaslukiolainen", "Ynjevi", "080698-967F")
  val ylioppilasEiOppilaitosta = oppijat.oppija("Ylioppilas", "Yrjänä", "240775-720P")
  val toimintaAlueittainOpiskelija = oppijat.oppija("Toiminta", "Tommi", "031112-020J")
  val telma = oppijat.oppija("Telmanen", "Tuula", "021080-725C")
  val erikoisammattitutkinto = oppijat.oppija("Erikoinen", "Erja", "250989-419V")
  val reformitutkinto = oppijat.oppija("Reformi", "Reijo", "251176-003P")
  val osittainenammattitutkinto = oppijat.oppija("Osittainen", "Outi", "230297-6448")
  val ammatillisenOsittainenRapsa = oppijat.oppija("Ammatillinen-Osittainen", "Raitsu", "140493-2798")
  val paikallinenTunnustettu = oppijat.oppija("Tunnustettu", "Teuvo", "140176-449X")
  val tiedonsiirto = oppijat.oppija("Tiedonsiirto", "Tiina", "270303-281N")
  val perusopetuksenTiedonsiirto = oppijat.oppija("Perusopetuksensiirto", "Pertti", "010100-071R")
  val omattiedot = oppijat.oppija(MockUsers.omattiedot.ldapUser.sukunimi, MockUsers.omattiedot.ldapUser.etunimet, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibFinal = oppijat.oppija("IB-final", "Iina", "040701-432D")
  val ibPredicted = oppijat.oppija("IB-predicted", "Petteri", "071096-317K")
  val dia = oppijat.oppija("Dia", "Dia", "151013-2195")
  val internationalschool = oppijat.oppija("International", "Ida", "170186-854H")
  val eskari = oppijat.oppija("Eskari", "Essi", "300996-870E")
  val master = oppijat.oppija("of Puppets", "Master", "101097-6107")
  val slave = oppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000051473", sukunimi = "of Puppets", etunimet = "Slave", kutsumanimi = "Slave", hetu = Some("101097-6107"), syntymäaika = None), Some(master)))
  val masterEiKoskessa = oppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = oppijat.generateId(), sukunimi = "Master", etunimet = "Master", kutsumanimi = "Master", hetu = Some("270366-697B"), syntymäaika = None))
  val slaveMasterEiKoskessa = oppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.41000051473", hetu = Some("270366-697B"), syntymäaika = None, sukunimi = "Slave", etunimet = "Slave", kutsumanimi = "Slave"), Some(masterEiKoskessa)))
  val omattiedotSlave = oppijat.addOppija(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = oppijat.generateId(), hetu = Some("190751-739W"), syntymäaika = None, etunimet = MockUsers.omattiedot.ldapUser.etunimet, kutsumanimi = MockUsers.omattiedot.ldapUser.etunimet, sukunimi = MockUsers.omattiedot.ldapUser.sukunimi), Some(omattiedot)))
  val opiskeluoikeudenOidKonflikti = oppijat.oppija("Oidkonflikti", "Oskari", "260539-745W", "1.2.246.562.24.09090909090")
  val eiKoskessa = oppijat.oppija("EiKoskessa", "Eino", "270181-5263", "1.2.246.562.24.99999555555", vanhaHetu = Some("270181-517T"))
  val eiKoskessaHetuton = oppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999555556", sukunimi = "EiKoskessaHetuton", etunimet = "Eino", kutsumanimi = "Eino", hetu = None, syntymäaika = None))
  val turvakielto = oppijat.oppija("Turvakielto", "Tero", "151067-2193", turvakielto = true)
  val montaJaksoaKorkeakoululainen = oppijat.oppija("Korkeakoululainen", "Monta-Opintojaksoa", "030199-3419")
  val organisaatioHistoria = oppijat.oppija("Historoitsija", "Hiisi", "200994-834A")
  val montaKoulutuskoodiaAmis = oppijat.oppija("Koodari", "Monthy", "151099-036E")
  val tunnisteenKoodiarvoPoistettu = oppijat.oppija("ePerusteidenKoulutuksen-koodi", "Poistettu", "161097-132N")
  val valtuutusOppija = oppijat.oppija("Sydänmaanlakka-Horttanainen", "Katariina Eeva Marjatta", "020190-9521", kutsumanimi = Some("Katariina"))
  val siirtoOpiskelijaVirta = oppijat.oppija("SiirtoOpiskelijaVirta", "Siiri", "141199-418X")
  val faija = oppijat.oppija("EiOpintojaKoskessa", "Faija", "030300-5215")
  val faijaFeilaa = oppijat.oppija("EiOpintojaKoskessaLastenHakuFailaa", "Faija", "030300-7053")
  val koulusivistyskieliYlioppilas = oppijat.oppija("Koulu", "SivistysKieli", "020401-368M")
  val montaKoulusivityskieltäYlioppilas = oppijat.oppija("MontaKoulu", "SivistysKieltä", "020401-746U")
  val labammattikoulu = oppijat.oppija("Lahti", "LAB", "260308-361W")
  val valviraaKiinnostavaTutkinto = oppijat.oppija("Valviralle", "Veera", "120100A2365")
  val valviraaKiinnostavaTutkintoKesken = oppijat.oppija("Valviralle-Kesken", "Ville", "131099-633D")
  val kelaErityyppisiaOpiskeluoikeuksia = oppijat.oppija("Kelalle", "Useita", "100800A057R")

  val virtaOppija = oppijat.addOppija(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.57060795845", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = Some("270191-4208"), syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None))
  val virtaOppijaHetuton = oppijat.addOppija(OppijaHenkilöWithMasterInfo(
      LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.20170814313", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = None, syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None),
      Some(virtaOppija)))

  def defaultOppijat = oppijat.getOppijat

  def generateOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)

  def oids = (defaultOppijat.map(_.henkilö.oid) ++ (1 to defaultOppijat.length + 100).map(generateOid).toList).distinct // oids that should be considered when deleting fixture data

  def asUusiOppija(oppija: LaajatOppijaHenkilöTiedot) =
    UusiHenkilö(oppija.hetu.get, oppija.etunimet, Some(oppija.kutsumanimi), oppija.sukunimi)
}

class MockOppijat(private var oppijat: List[OppijaHenkilöWithMasterInfo] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String, oid: String = generateId(), kutsumanimi: Option[String] = None, turvakielto: Boolean = false, vanhaHetu: Option[String] = None, sukupuoli: Option[String] = None, kotikunta: Option[String] = None): LaajatOppijaHenkilöTiedot =
    addOppija(henkilo.LaajatOppijaHenkilöTiedot(
      oid = oid,
      sukunimi = suku,
      etunimet = etu,
      kutsumanimi = kutsumanimi.getOrElse(etu),
      hetu = Some(hetu),
      syntymäaika = None,
      äidinkieli = Some("fi"),
      turvakielto = turvakielto,
      vanhatHetut = vanhaHetu.toList,
      sukupuoli = sukupuoli,
      kotikunta = kotikunta
    ))

  def addOppija(oppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = addOppija(OppijaHenkilöWithMasterInfo(oppija, None)).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]

  def addOppija(oppija: OppijaHenkilöWithMasterInfo): OppijaHenkilöWithMasterInfo = {
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    MockOppijat.generateOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}
