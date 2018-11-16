package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUserLanguage.{getLanguageFromCookie, sanitizeLanguage, setLanguageCookie}

trait LanguageSupport extends KoskiBaseServlet {
  def application: KoskiApplication

  def lang: String = getLanguageFromCookie(request)
  def t(key: String): String = application.localizationRepository.get(key).get(lang)

  def langFromDomain: String = if (request.getServerName == swedishDomain) {
    "sv"
  } else {
    "fi"
  }

  def langFromCookie(allowEnglish: Boolean = false): Option[String] = sanitizeLanguage(request.cookies.get("lang"), allowEnglish)

  def setLangCookieFromDomainIfNecessary(allowEnglish: Boolean = false): Unit = if (langFromCookie(allowEnglish).isEmpty) {
    setLanguageCookie(langFromDomain, response)
  }

  private def swedishDomain = application.config.getString("koski.oppija.domain.sv")
}
