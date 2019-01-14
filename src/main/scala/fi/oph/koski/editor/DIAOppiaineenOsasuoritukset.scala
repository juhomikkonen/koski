package fi.oph.koski.editor

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema._

case class DIAOppiaineenOsasuoritukset(koodistoViitePalvelu: KoodistoViitePalvelu) {
  def osasuoritukset(suorituksenTyyppi: String) = {
    suorituksenTyyppi match {
      case _ => oppiaineenOsasuoritukset
    }
  }

  private lazy val oppiaineenOsasuoritukset = List(
    DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(
      koulutusmoduuli = DIAOppiaineenTutkintovaiheenLukukausi(
        tunniste = Koodistokoodiviite(koodiarvo = "3", koodistoUri = "dialukukausi"),
        laajuus = None
      )
    )
  )
}
