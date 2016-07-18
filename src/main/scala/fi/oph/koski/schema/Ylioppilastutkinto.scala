package fi.oph.koski.schema

import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems}

case class YlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[OrganisaatioWithOid],
  tila: YlioppilastutkinnonOpiskeluoikeudenTila,
  @MinItems(1)
  @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", "opiskeluoikeudentyyppi")
) extends Opiskeluoikeus {
  override def withKoulutustoimija(koulutustoimija: OrganisaatioWithOid) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def alkamispäivä = None
  override def päättymispäivä = None
  override def id = None
  override def versionumero = None
  override def läsnäolotiedot = None
  override def lähdejärjestelmänId = None
}

case class YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]) extends OpiskeluoikeudenTila

case class YlioppilastutkinnonSuoritus(
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = None),
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  vahvistus: Option[Organisaatiovahvistus] = None,
  @Description("Ylioppilastutkinnon kokeiden suoritukset")
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi")
) extends Suoritus with Toimipisteellinen {
  def arviointi: Option[List[KoodistostaLöytyväArviointi]] = None
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilastutkinnonKokeenSuoritus(
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  tila: Koodistokoodiviite,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi")
) extends Suoritus {
  def vahvistus = None
  override def tarvitseeVahvistuksen = false
  override def suorituskieli: Option[Koodistokoodiviite] = None
}

case class YlioppilaskokeenArviointi(
  @KoodistoUri("koskiyoarvosanat")
  arvosana: Koodistokoodiviite
) extends KoodistostaLöytyväArviointi {
  override def arviointipäivä = None
  override def arvioitsijat = None
  def hyväksytty = arvosana.koodiarvo != "I"
}

object YlioppilaskokeenArviointi {
  def apply(arvosana: String) = new YlioppilaskokeenArviointi(Koodistokoodiviite(arvosana, "koskiyoarvosanat"))
}

@Description("Ylioppilastutkinnon tunnistetiedot")
case class Ylioppilastutkinto(
 @KoodistoKoodiarvo("301000")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
 perusteenDiaarinumero: Option[String]
) extends Koulutus with EPerusteistaLöytyväKoulutusmoduuli {
  override def laajuus = None
  override def isTutkinto = true
}

@Description("Ylioppilastutkinnon kokeen tunnistetiedot")
case class YlioppilasTutkinnonKoe(
  tunniste: PaikallinenKoodi
) extends PaikallinenKoulutusmoduuli {
  def laajuus = None
}