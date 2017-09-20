package fi.oph.koski.api

import fi.oph.koski.documentation.ExampleData.{suomenKieli, _}
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema._

// Lukiosuoritusten validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.
class OppijaValidationPerusopetuksenOppiaineenOppimaaraSpec extends TutkinnonPerusteetTest[AikuistenPerusopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAikuistenPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = AikuistenPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = MuuPeruskoulunOppiaine(
          tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "HI"),
          perusteenDiaarinumero = diaari
        ),
        tila = tilaValmis,
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = PerusopetusExampleData.arviointi(9),
        suoritustapa = PerusopetusExampleData.suoritustapaErityinenTutkinto,
        vahvistus = vahvistus,
        suorituskieli = suomenKieli
      )
    ),
    alkamispäivä = Some(longTimeAgo),
    tila = PerusopetuksenOpiskeluoikeudenTila(List(PerusopetuksenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä)))
  )

  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
}