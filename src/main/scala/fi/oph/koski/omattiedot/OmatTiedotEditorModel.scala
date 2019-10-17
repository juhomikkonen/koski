package fi.oph.koski.omattiedot

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.oppilaitoksenOpiskeluoikeudetOrdering
import fi.oph.koski.editor._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.{Timing, WithWarnings}
import fi.oph.scalaschema.annotation.SyntheticProperty
import mojave._

object OmatTiedotEditorModel extends Timing {
  def toEditorModel(userOppija: WithWarnings[Oppija], oppija: Option[WithWarnings[Oppija]] = None)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    val piilotetuillaTiedoilla = piilotaArvosanatKeskeneräisistäSuorituksista _ andThen
      piilotaSensitiivisetHenkilötiedot andThen
      piilotaKeskeneräisetPerusopetuksenPäättötodistukset

    val warnings = userOppija.warnings ++ oppija.toList.flatMap(_.warnings)
    buildModel(buildView(piilotetuillaTiedoilla(userOppija.getIgnoringWarnings), oppija.map(h => piilotetuillaTiedoilla(h.getIgnoringWarnings)), warnings))
  }

  def opiskeluoikeudetOppilaitoksittain(oppija: Oppija): List[OppilaitoksenOpiskeluoikeudet] = {
    oppija.opiskeluoikeudet.groupBy(_.getOppilaitosOrKoulutusToimija).map {
      case (oppilaitos, opiskeluoikeudet) => OppijaEditorModel.toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
    }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
  }

  private def buildView(userOppija: Oppija, oppija: Option[Oppija], warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSession) = {
    val huollettavat = application.huollettavatService.getHuollettavatWithOid(koskiSession.oid).map(_.toHenkilötiedotJaOid)
    val valittuOppija = oppija.getOrElse(userOppija)
    val henkilö = valittuOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot]
    val userHenkilö = userOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot]
    OmatTiedotEditorView(
      henkilö = valittuOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
      userHenkilö = HenkilötiedotJaOid(userHenkilö.oid, userHenkilö.hetu, userHenkilö.etunimet, userHenkilö.kutsumanimi, userHenkilö.sukunimi),
      huollettavat = huollettavat,
      opiskeluoikeudet = opiskeluoikeudetOppilaitoksittain(valittuOppija),
      varoitukset = warnings.flatMap(_.errors).map(_.key).toList
    )
  }

  private def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  private def piilotaArvosanatKeskeneräisistäSuorituksista(oppija: Oppija) = {
    val keskeneräisetTaiLiianÄskettäinVahvistetut = traversal[Suoritus].filter { s =>
      s.vahvistus.isEmpty || !s.vahvistus.exists { v => v.päivä.plusDays(4).isBefore(LocalDate.now())}
    }.compose(päätasonSuorituksetTraversal)
    val piilotettavatOppiaineidenArvioinnit = (oppimääränArvioinnitTraversal ++ vuosiluokanArvioinnitTraversal ++ oppiaineenOppimääränArvioinnitTraversal).compose(keskeneräisetTaiLiianÄskettäinVahvistetut)
    val piilotettavaKäyttäytymisenArviointi = käyttäytymisenArviointiTraversal.compose(keskeneräisetTaiLiianÄskettäinVahvistetut)

    List(piilotettavaKäyttäytymisenArviointi, piilotettavatOppiaineidenArvioinnit).foldLeft(oppija) { (oppija, traversal) =>
      traversal.set(oppija)(None)
    }
  }

  private def piilotaSensitiivisetHenkilötiedot(oppija: Oppija) = {
    val t: Traversal[Oppija, TäydellisetHenkilötiedot] = traversal[Oppija].field[Henkilö]("henkilö").ifInstanceOf[TäydellisetHenkilötiedot]
    t.modify(oppija)((th: TäydellisetHenkilötiedot) => th.copy(hetu = None, kansalaisuus = None, turvakielto = None))
  }

  def piilotaKeskeneräisetPerusopetuksenPäättötodistukset(oppija: Oppija): Oppija = {
    def poistaKeskeneräisetPäättötodistukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.filter(_ match {
      case s: PerusopetuksenOppimääränSuoritus if !s.valmis => false
      case _ => true
    })

    def poistaOsasuoritukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.map(s =>
      shapeless.lens[PäätasonSuoritus].field[Option[List[Suoritus]]]("osasuoritukset").set(s)(None)
    )

    shapeless.lens[Oppija].field[Seq[Opiskeluoikeus]]("opiskeluoikeudet").modify(oppija)(_.map(oo => {
      val isKeskeneräinenPäättötodistusAinoaSuoritus = oo.suoritukset match {
        case (s: PerusopetuksenOppimääränSuoritus) :: Nil if s.kesken => true
        case _ => false
      }

      shapeless.lens[Opiskeluoikeus].field[List[PäätasonSuoritus]]("suoritukset").modify(oo)(
        if (isKeskeneräinenPäättötodistusAinoaSuoritus) poistaOsasuoritukset else poistaKeskeneräisetPäättötodistukset
      )
    }))
  }
}

case class OmatTiedotEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  @Hidden
  userHenkilö: HenkilötiedotJaOid,
  @Hidden
  huollettavat: List[HenkilötiedotJaOid],
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet],
  @Hidden
  varoitukset: List[String]
) {
  @Hidden @SyntheticProperty
  def kaikkiHenkilöt: List[HenkilötiedotJaOid] = userHenkilö :: huollettavat
  @Hidden @SyntheticProperty
  def hasHuollettavia: Boolean = huollettavat.nonEmpty
}
