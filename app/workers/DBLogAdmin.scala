package workers

import javax.inject.{Inject, Singleton}

import akka.actor.Actor
import models.AppLogger
import models.daos.LoggerDAO
import play.Logger


/**
 * Created by alex on 27/09/15.
 */
@Singleton
class DBLogAdmin @Inject()(loggerDAO: LoggerDAO)  extends Actor {

  def receive = {
    case log: AppLogger =>
      try {
        loggerDAO.insert(log)
      } catch {
        case t: Throwable =>
          Logger.error("Log could not be written", t)
      }
  }

}
