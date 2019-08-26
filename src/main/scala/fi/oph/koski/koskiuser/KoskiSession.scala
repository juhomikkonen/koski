package fi.oph.koski.koskiuser

import java.net.InetAddress

import fi.oph.koski.json.FilteringCriteria
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio, OrganisaatioWithOid}
import org.scalatra.servlet.RichRequest

import scala.concurrent.{ExecutionContext, Future}

class KoskiSession(val user: AuthenticationUser, val lang: String, val clientIp: InetAddress, val userAgent: String, käyttöoikeudet: => Set[Käyttöoikeus]) extends LogUserContext with UserWithUsername with UserWithOid with FilteringCriteria  with Loggable with Logging {
  def oid = user.oid
  def username = user.username
  def userOption = Some(user)
  def logString = "käyttäjä " + username + " / " + user.oid

  lazy val orgKäyttöoikeudet: Set[KäyttöoikeusOrg] = käyttöoikeudet.collect { case k : KäyttöoikeusOrg => k}
  lazy val globalKäyttöoikeudet: Set[KäyttöoikeusGlobal] = käyttöoikeudet.collect { case k: KäyttöoikeusGlobal => k}
  lazy val globalViranomaisKäyttöoikeudet: Set[KäyttöoikeusViranomainen] = käyttöoikeudet.collect { case k: KäyttöoikeusViranomainen => k}
  lazy val allowedOpiskeluoikeusTyypit: Set[String] = käyttöoikeudet.flatMap(_.allowedOpiskeluoikeusTyypit)
  lazy val hasKoulutusmuotoRestrictions: Boolean = allowedOpiskeluoikeusTyypit != OpiskeluoikeudenTyyppi.kaikkiTyypit.map(_.koodiarvo)

  def organisationOids(accessType: AccessType.Value): Set[String] = orgKäyttöoikeudet.collect { case k: KäyttöoikeusOrg if k.organisaatioAccessType.contains(accessType) => k.organisaatio.oid }
  lazy val globalAccess = globalKäyttöoikeudet.flatMap { _.globalAccessType }
  def isRoot = globalAccess.contains(AccessType.write)
  def isPalvelukäyttäjä = orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit).contains(Palvelurooli(TIEDONSIIRTO))
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write) && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
  def hasTiedonsiirronMitätöintiAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.tiedonsiirronMitätöinti)
  def hasLuovutuspalveluAccess: Boolean = globalViranomaisKäyttöoikeudet.exists(_.isLuovutusPalveluAllowed)
  def hasTilastokeskusAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", TILASTOKESKUS))

  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = {
    val access = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
    access && (accessType != AccessType.write || hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  }

  def hasGlobalKoulutusmuotoReadAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalAccessType).contains(AccessType.read)

  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)
  def hasAnyWriteAccess = (globalAccess.contains(AccessType.write) || organisationOids(AccessType.write).nonEmpty) && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
  def hasLocalizationWriteAccess = globalKäyttöoikeudet.find(_.globalPalveluroolit.contains(Palvelurooli("LOKALISOINTI", "CRUD"))).isDefined
  def hasAnyReadAccess = hasGlobalReadAccess || orgKäyttöoikeudet.exists(_.organisaatioAccessType.contains(AccessType.read)) || hasGlobalKoulutusmuotoReadAccess
  def hasAnyTiedonsiirronMitätöintiAccess = globalAccess.contains(AccessType.tiedonsiirronMitätöinti) || organisationOids(AccessType.tiedonsiirronMitätöinti).nonEmpty
  def hasRaportitAccess = hasAnyReadAccess && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT) && !hasGlobalKoulutusmuotoReadAccess
  def sensitiveDataAllowed(requiredRoles: Set[Role]) = requiredRoles.exists(hasRole)
  def viranomaisOrganisaatiot: Set[OrganisaatioWithOid] = globalViranomaisKäyttöoikeudet.map(_.organisaatio)

  // Note: keep in sync with PermissionCheckServlet's hasSufficientRoles function. See PermissionCheckServlet for more comments.
  private val OppijanumerorekisteriRekisterinpitäjä = Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")
  private val OppijanumerorekisteriReadUpdate = Palvelurooli("OPPIJANUMEROREKISTERI", "HENKILON_RU")
  def hasHenkiloUiWriteAccess = globalKäyttöoikeudet.exists(ko => ko.globalPalveluroolit.contains(OppijanumerorekisteriRekisterinpitäjä) || ko.globalPalveluroolit.contains(OppijanumerorekisteriReadUpdate)) ||
    orgKäyttöoikeudet.exists(ko => ko.organisaatiokohtaisetPalveluroolit.contains(OppijanumerorekisteriRekisterinpitäjä) || ko.organisaatiokohtaisetPalveluroolit.contains(OppijanumerorekisteriReadUpdate))

  def hasRole(role: String): Boolean = {
    val palveluRooli = Palvelurooli("KOSKI", role)
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(palveluRooli)) ||
    globalViranomaisKäyttöoikeudet.exists(_.globalPalveluroolit.contains(palveluRooli)) ||
    orgKäyttöoikeudet.exists(_.organisaatiokohtaisetPalveluroolit.contains(palveluRooli))
  }

  def juuriOrganisaatiot: List[OrganisaatioWithOid] = orgKäyttöoikeudet.collect { case r: KäyttöoikeusOrg if r.juuri => r.organisaatio }.toList

  Future(käyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object KoskiSession {
  def apply(user: AuthenticationUser, request: RichRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiSession = {
    new KoskiSession(user, KoskiUserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  private val KOSKI_SYSTEM_USER: String = "Koski system user"
  private val UNTRUSTED_SYSTEM_USER = "Koski untrusted system user"
  val SUORITUSJAKO_KATSOMINEN_USER = "Koski suoritusjako katsominen"
  // Internal user with root access
  val systemUser = new KoskiSession(AuthenticationUser(KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)))))

  val untrustedUser = new KoskiSession(AuthenticationUser(UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set())

  def suoritusjakoKatsominenUser(request: RichRequest) = new KoskiSession(AuthenticationUser(SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, None), KoskiUserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))))
}

