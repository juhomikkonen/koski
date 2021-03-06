package fi.oph.koski.db

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.Environment
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.executors.Pools
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.Schema
import fi.oph.koski.util.Futures
import org.flywaydb.core.Flyway
import org.postgresql.util.PSQLException
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.sys.process._

object KoskiDatabase {
  type DB = PostgresProfile.backend.DatabaseDef

  def master(config: Config): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config))

  def replica(config: Config, master: KoskiDatabase): KoskiDatabase =
    new KoskiDatabase(KoskiDatabaseConfig(config, readOnly = true))
}

case class KoskiDatabaseConfig(c: Config, readOnly: Boolean = false, raportointiSchema: Option[Schema] = None) {
  private val baseConfig = c.getConfig("db").withoutPath("replica").withoutPath("raportointi")
  private val configFromFile = (raportointiSchema, readOnly) match {
    case (Some(schema), _) =>
      c.getConfig("db.raportointi")
        .withValue("poolName", fromAnyRef(s"koskiRaportointiPool-${schema.name}"))
        .withFallback(baseConfig)
    case (_, false) => baseConfig
    case (_, true) => c.getConfig("db.replica").withFallback(baseConfig)
  }
  private val otherConfig = ConfigFactory.empty
    .withValue("url", fromAnyRef("jdbc:postgresql://" + configFromFile.getString("host") + ":" + configFromFile.getInt("port") + "/" + configFromFile.getString("name")))
    .withValue("readOnly", fromAnyRef(readOnly))
    .withValue("numThreads", fromAnyRef(Pools.dbThreads))

  private val config = configFromFile.withFallback(otherConfig)

  def dbName = config.getString("name")
  def user = config.getString("user")
  def password = config.getString("password")
  def host = config.getString("host")
  def port = config.getInt("port")
  def url = config.getString("url")

  def isLocal = host == "localhost"
  def isRemote = !isLocal
  def toSlickDatabase = Database.forConfig("", config)
}


class KoskiDatabase(val config: KoskiDatabaseConfig) extends Logging {
  val serverProcess = startLocalDatabaseServerIfNotRunning

  if (!config.isRemote && !config.readOnly) {
    createDatabase
    createUser
  }

  val db: DB = config.toSlickDatabase

  if (!config.readOnly) {
    migrateSchema
  }

  private def startLocalDatabaseServerIfNotRunning: Option[PostgresRunner] = {
    if (config.isLocal && !config.readOnly) {
      Some(new PostgresRunner("postgresql/data", "postgresql/postgresql.conf", config.port).start)
    } else {
      None
    }
  }

  private def createDatabase = {
    val dbName = config.dbName
    val port = config.port
    s"createdb -p $port -T template0 -E UTF-8 $dbName".!
  }

  private def createUser = {
    val user = config.user
    val port = config.port
    s"createuser -p $port -s $user -w".!
  }

  private def migrateSchema = {
    try {
      val flyway = new Flyway
      flyway.setDataSource(config.url, config.user, config.password)
      flyway.setSchemas(config.user)
      flyway.setValidateOnMigrate(false)
      if (System.getProperty("koski.db.clean", "false").equals("true")) {
        flyway.clean
      }
      if (Environment.isLocalDevelopmentEnvironment && databaseIsLarge) {
        logger.warn("Skipping database migration for database larger than 100 rows, when running in local development environment")
      } else {
        flyway.migrate
      }
    } catch {
      case e: Exception => logger.warn(e)("Migration failure")
    }
  }

  def databaseIsLarge: Boolean = {
    try {
      val count = Futures.await(db.run(sql"select count(id) from opiskeluoikeus".as[Int]))(0)
      count > 100
    } catch {
      case e: PSQLException =>
        if (e.getMessage.contains("""relation "opiskeluoikeus" does not exist""")) {
          false // Allow for an uninitialized db
        } else {
          throw e
        }
    }
  }
}




