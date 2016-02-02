package models

import java.sql.Timestamp
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Logentry (rootid: String,
                      title: String,
                      exception: String,
                      stacktrace: String,
                      req_header: String,
                      req_method: String,
                      req_address: String,
                      req_uri: String)

/**
  * The companion object.
  */
object Logentry {

  import utils.Utils.Implicits._

  /**
    * Converts the [Offer] object to Json and vice versa.
    */
  implicit val jsonFormat = Json.format[Logentry]
}

trait LoggerService  {

  def create(logger: Logentry): Future[Logentry]

}

class LoggerServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends LoggerService with DAOSlick {

  import driver.api._

  override def create(logger: Logentry): Future[Logentry] = {
    db.run(slickLoggers += DBLogger(None,logger.rootid, logger.title,logger.exception,logger.stacktrace, logger.req_header, logger.req_method, logger.req_address, logger.req_uri,new Timestamp(System.currentTimeMillis))).map(_ => logger)
  }

}
