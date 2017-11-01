package fi.oph.koski.perustiedot

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.PerustiedotSync
import fi.oph.koski.db.{GlobalExecutionContext, KoskiDatabaseMethods, PerustiedotSyncRow}
import rx.lang.scala.Observable
import fi.oph.koski.json.JsonSerializer
import slick.dbio.Effect
import slick.sql.FixedSqlAction

class PerustiedotSyncRepository(val db: DB) extends GlobalExecutionContext with KoskiDatabaseMethods {
  def syncLater(opiskeluoikeudet: Seq[OpiskeluoikeudenOsittaisetTiedot], upsert: Boolean) = runDbSync(PerustiedotSync ++= opiskeluoikeudet.map { oo => PerustiedotSyncRow(opiskeluoikeusId = oo.id, data = JsonSerializer.serializeWithRoot(oo), upsert = upsert)})

  def syncAction(perustiedot: OpiskeluoikeudenOsittaisetTiedot, upsert: Boolean): FixedSqlAction[Int, NoStream, Effect.Write] = PerustiedotSync += PerustiedotSyncRow(opiskeluoikeusId = perustiedot.id, data = JsonSerializer.serializeWithRoot(perustiedot), upsert = upsert)

  def needSyncing: Observable[PerustiedotSyncRow] = {
    streamingQuery(PerustiedotSync)
  }

  def delete(ids: Seq[Int]): Int =
    runDbSync(PerustiedotSync.filter(_.id inSetBind ids).delete)
}

