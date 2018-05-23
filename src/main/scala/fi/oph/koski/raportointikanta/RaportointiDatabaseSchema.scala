package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import slick.dbio.DBIO

object RaportointiDatabaseSchema {

  val createIndexes = DBIO.seq(
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppija_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(oppilaitos_oid)",
    sqlu"CREATE INDEX ON r_opiskeluoikeus(koulutusmuoto)",
    sqlu"CREATE INDEX ON r_paatason_suoritus(opiskeluoikeus_oid)",
    sqlu"CREATE INDEX ON r_henkilo(hetu)",
    sqlu"CREATE INDEX ON r_organisaatio(oppilaitosnumero)",
    sqlu"CREATE UNIQUE INDEX ON r_koodisto_koodi(koodisto_uri, koodiarvo)"
  )

  val dropAllIfExists = DBIO.seq(
    sqlu"DROP TABLE IF EXISTS r_opiskeluoikeus",
    sqlu"DROP TABLE IF EXISTS r_paatason_suoritus",
    sqlu"DROP TABLE IF EXISTS r_henkilo",
    sqlu"DROP TABLE IF EXISTS r_organisaatio",
    sqlu"DROP TABLE IF EXISTS r_koodisto_koodi"
  )

  class ROpiskeluoikeusTable(tag: Tag) extends Table[ROpiskeluoikeusRow](tag, "r_opiskeluoikeus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid", O.PrimaryKey)
    val versionumero = column[Int]("versionumero")
    val aikaleima = column[Timestamp]("aikaleima")
    val oppijaOid = column[String]("oppija_oid")
    val oppilaitosOid = column[String]("oppilaitos_oid")
    val koulutustoimijaOid = column[String]("koulutustoimija_oid")
    val koulutusmuoto = column[String]("koulutusmuoto")
    def * = (opiskeluoikeusOid, versionumero, aikaleima, oppijaOid, oppilaitosOid, koulutustoimijaOid, koulutusmuoto) <> (ROpiskeluoikeusRow.tupled, ROpiskeluoikeusRow.unapply)
  }

  class RPäätasonSuoritusTable(tag: Tag) extends Table[RPäätasonSuoritusRow](tag, "r_paatason_suoritus") {
    val opiskeluoikeusOid = column[String]("opiskeluoikeus_oid")
    val suorituksenTyyppi = column[String]("suorituksen_tyyppi")
    val koulutusmoduuliKoodisto = column[Option[String]]("koulutusmoduuli_koodisto")
    val koulutusmoduuliKoodiarvo = column[String]("koulutusmoduuli_koodiarvo")
    val koulutustyyppi = column[Option[String]]("koulutustyyppi")
    val vahvistusPäivä = column[Option[Date]]("vahvistus_paiva")
    def * = (opiskeluoikeusOid, suorituksenTyyppi, koulutusmoduuliKoodisto, koulutusmoduuliKoodiarvo, koulutustyyppi, vahvistusPäivä) <> (RPäätasonSuoritusRow.tupled, RPäätasonSuoritusRow.unapply)
  }

  class RHenkilöTable(tag: Tag) extends Table[RHenkilöRow](tag, "r_henkilo") {
    val oppijaOid = column[String]("oppija_oid", O.PrimaryKey)
    val hetu = column[Option[String]]("hetu")
    val sukunimi = column[String]("sukunimi")
    val etunimet = column[String]("etunimet")
    val äidinkieli = column[Option[String]]("aidinkieli")
    val kansalaisuus = column[Option[String]]("kansalaisuus")
    val turvakielto = column[Boolean]("turvakielto")
    def * = (oppijaOid, hetu, sukunimi, etunimet, äidinkieli, kansalaisuus, turvakielto) <> (RHenkilöRow.tupled, RHenkilöRow.unapply)
  }

  class ROrganisaatioTable(tag: Tag) extends Table[ROrganisaatioRow](tag, "r_organisaatio") {
    val organisaatioOid = column[String]("organisaatio_oid", O.PrimaryKey)
    val nimi = column[String]("nimi")
    val organisaatiotyypit = column[String]("organisaatiotyypit")
    val oppilaitostyyppi = column[Option[String]]("oppilaitostyyppi")
    val oppilaitosnumero = column[Option[String]]("oppilaitosnumero")
    def * = (organisaatioOid, nimi, organisaatiotyypit, oppilaitostyyppi, oppilaitosnumero) <> (ROrganisaatioRow.tupled, ROrganisaatioRow.unapply)
  }

  class RKoodistoKoodiTable(tag: Tag) extends Table[RKoodistoKoodiRow](tag, "r_koodisto_koodi") {
    val koodistoUri = column[String]("koodisto_uri")
    val koodiarvo = column[String]("koodiarvo")
    val nimi = column[String]("nimi")
    def * = (koodistoUri, koodiarvo, nimi) <> (RKoodistoKoodiRow.tupled, RKoodistoKoodiRow.unapply)
  }

}

case class ROpiskeluoikeusRow(
  opiskeluoikeusOid: String,
  versionumero: Int,
  aikaleima: Timestamp,
  oppijaOid: String,
  oppilaitosOid: String,
  koulutustoimijaOid: String,
  koulutusmuoto: String
)

case class RPäätasonSuoritusRow(
  opiskeluoikeusOid: String,
  suorituksenTyyppi: String,
  koulutusmoduuliKoodisto: Option[String],
  koulutusmoduuliKoodiarvo: String,
  koulutustyyppi: Option[String],
  vahvistusPäivä: Option[Date]
)

case class RHenkilöRow(
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  aidinkieli: Option[String],
  kansalaisuus: Option[String],
  turvakielto: Boolean
)

case class ROrganisaatioRow(
  organisaatioOid: String,
  nimi: String,
  organisaatiotyypit: String,
  oppilaitostyyppi: Option[String],
  oppilaitosnumero: Option[String]
)

case class RKoodistoKoodiRow(
  koodistoUri: String,
  koodiarvo: String,
  nimi: String
)