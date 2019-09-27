package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.schema._


object OpiskeluoikeusChangeMigrator {
  def migrate(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowDeleteCompleted: Boolean): KoskeenTallennettavaOpiskeluoikeus = {
    val uusiOpiskeluoikeusSuorituksilla = if (allowDeleteCompleted) uusiOpiskeluoikeus else kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)
    organisaationMuutosHistoria(vanhaOpiskeluoikeus, uusiOpiskeluoikeusSuorituksilla)
  }

  private def kopioiValmiitSuorituksetUuteen(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    if (OpiskeluoikeudenTyyppi.ammatillinenkoulutus == uusiOpiskeluoikeus.tyyppi) {
      uusiOpiskeluoikeus
    } else {
      val puuttuvatSuorituksetUudessa = vanhaOpiskeluoikeus.suoritukset
        .filter(kopioitavaPäätasonSuoritus)
        .filter { vanhaSuoritus =>
          vanhaSuoritus.valmis && !uusiOpiskeluoikeus.suoritukset.exists(_.koulutusmoduuli.tunniste == vanhaSuoritus.koulutusmoduuli.tunniste)
        }
      uusiOpiskeluoikeus.withSuoritukset(puuttuvatSuorituksetUudessa ++ uusiOpiskeluoikeus.suoritukset)
    }
  }

  private def organisaationMuutosHistoria(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    if (oppilaitoksenTaiKoulutustoimijanOidMuuttunut(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)) {
      val vanhaHistoria = vanhaOpiskeluoikeus.organisaatiohistoria.toList.flatten
      val muutos = OpiskeluoikeudenOrganisaatiohistoria(LocalDate.now(), vanhaOpiskeluoikeus.oppilaitos.get, vanhaOpiskeluoikeus.koulutustoimija.get)
      uusiOpiskeluoikeus.withHistoria(Some(vanhaHistoria :+ muutos))
    } else {
      uusiOpiskeluoikeus.withHistoria(vanhaOpiskeluoikeus.organisaatiohistoria)
    }
  }

  private def kopioitavaPäätasonSuoritus(suoritus: KoskeenTallennettavaPäätasonSuoritus) = suoritus match {
    case _: LukionOppiaineenOppimääränSuoritus |
         _: NuortenPerusopetuksenOppiaineenOppimääränSuoritus |
         _: AikuistenPerusopetuksenOppiaineenOppimääränSuoritus => false
    case _ => true
  }

  private def oppilaitoksenTaiKoulutustoimijanOidMuuttunut(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    !(vanhaOpiskeluoikeus.oppilaitos.map(_.oid) == uusiOpiskeluoikeus.oppilaitos.map(_.oid) &&
      vanhaOpiskeluoikeus.koulutustoimija.map(_.oid) == uusiOpiskeluoikeus.koulutustoimija.map(_.oid))
  }
}
