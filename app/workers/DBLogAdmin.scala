package workers

import javax.inject.{Inject, Singleton}

import akka.actor.Actor
import models.{Logentry, LoggerService}
import play.Logger
import play.api.Configuration


/**
 * Created by alex on 27/09/15.
 */
@Singleton
class DBLogAdmin @Inject()(conf: Configuration, service: LoggerService) extends Actor {

  def receive = {
    case log: Logentry =>
      try {
        service.create(log)
      } catch {
        case t: Throwable =>
          Logger.error("Log could not be written", t)
      }
  }

}
