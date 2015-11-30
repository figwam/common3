package models.daos

import java.sql.Timestamp
import javax.inject.Inject


import models.AppLogger
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait LoggerDAO  {

  def insert(logger: AppLogger): Future[AppLogger]

}

class LoggerDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends LoggerDAO with DAOSlick {

  import driver.api._


  override def insert(logger: AppLogger): Future[AppLogger] = {
    db.run(slickLoggers += DBLogger(None,logger.rootid, logger.title,logger.exception,logger.stacktrace, logger.req_header, logger.req_method, logger.req_address, logger.req_uri,new Timestamp(System.currentTimeMillis))).map(_ => logger)
  }

}
