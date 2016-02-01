package workers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, Cancellable}
import models.{ClazzService, ClazzDefinitionService, Clazz}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.libs.Json

import utils._

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by alex on 27/09/15.
 */
@Singleton
class ClazzScheduler @Inject()(cService: ClazzService, cdService: ClazzDefinitionService)  extends Actor {

  private val CREATE_CLAZZES = "CREATE_CLAZZES"

  private var scheduler: Cancellable = _

  override def preStart(): Unit = {
    import scala.concurrent.duration._
    lazy val refreshInterval:Int = 10 //Play.application().configuration().getString("clazz.definition.refresh.intervall").toInt
    scheduler = context.system.scheduler.schedule(
      initialDelay = refreshInterval.seconds,
      interval = refreshInterval.seconds,
      receiver = self,
      message = CREATE_CLAZZES
    )
  }



  override def postStop(): Unit = {
    scheduler.cancel()
  }

  def receive = {
    case CREATE_CLAZZES =>
      try {
        Logger.info("Execute Cron "+CREATE_CLAZZES)
        lazy val seeInAdvanceDays = 25 //Play.application().configuration().getString("days.see.clazzes.in.advance").toInt
        val clazzes =  cdService.listActive()
        Logger.info("Execute Cron "+CREATE_CLAZZES+":"+Json.toJson(clazzes))
        clazzes.map { clazzDef =>
          clazzDef.map { clazzDef =>
            Logger.debug(clazzDef.id+"-"+clazzDef.recurrence)
            Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays).map( clazz =>
              clazz match {
                case clazz: Clazz => {
                  clazzDef.id match {
                    case idClazzDef:Some[UUID] => {
                      val future = cService.create(clazz.copy(idClazzDef=idClazzDef.get)).map(c => Logger.debug("Create clazzes inserted, with id="+c.id))
                      future.onSuccess { case a => Logger.debug(s"Class created: $a") }
                      future.onFailure {
                        case t: PSQLException => {
                          if (t.getMessage.contains("duplicate key value violates unique constraint")) Logger.info("Class already exists")
                          else Logger.error("Something bad happened", t)
                        }
                        case t: Throwable => Logger.error(t.getMessage,t)
                      }
                    }
                    case _ => UUID.randomUUID()}

                }
                case _ => Logger.warn("outdated clazz definition found, id="+clazzDef.id)
              }
            )
            /*
            clazzDef.recurrence match {
              case (WEEKLY) => {
                Logger.debug("Create weekly class"+Json.toJson(clazzDef))

                Utils.calculateNextClazzes(clazzDef, seeInAdvanceDays).map( clazz =>
                  clazz match {
                    case clazz: Clazz => {
                      val future = cService.insert(clazz).map(c => Logger.debug("Create clazzes inserted, with id="+c.id))
                      future.onSuccess { case a => Logger.debug(s"Class created: $a") }
                      future.onFailure {
                        case t: PSQLException => Logger.warn("Class already exists")
                        case t: Throwable => Logger.error(t.getMessage,t)
                      }
                    }
                    case _ => Logger.warn("outdated clazz definition found, id="+clazzDef.id)
                  }
                )
              }
              case (ONETIME) => {}
              case _ => Logger.warn("Recurrence type unknown: "+clazzDef.recurrence);
            }

          */
          }

        }
        Logger.info("Finished Cron "+CREATE_CLAZZES+":"+Json.toJson(clazzes))
        clazzes.onSuccess { case a => Logger("Classes created")/*case a => Logger.debug(s"Classes created: $a")*/ }
        clazzes.onFailure { case t: Throwable => Logger.error(t.getMessage,t) }
      } catch {
        case t: Throwable =>
          Logger.error(t.getMessage,t)
      }
  }

}