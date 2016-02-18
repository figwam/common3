package controllers

import java.sql.Timestamp
import java.util.{GregorianCalendar, UUID}
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import akka.actor.ActorRef
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc.Result
import utils.FormValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The basic clazz def controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class ClazzController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       cService: ClazzService)
  extends Silhouette[User, JWTAuthenticator] {


  def clazzesPersonalizedAll(page: Int, orderBy: Int, filter: String) = SecuredAction.async { implicit request =>
    cService.listPersonalizedAll(page, 10, orderBy, "%" + filter + "%", request.identity.id.get).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }



  def clazzesPersonalizedMy(page: Int, orderBy: Int, filter: String, startFrom: Long, endAt:Long) = SecuredAction.async { implicit request =>
    val d = new GregorianCalendar()
    d.setTimeInMillis(startFrom)
    cService.listPersonalizedMy(page, 10, orderBy, "%" + filter + "%", request.identity.id.get, new Timestamp(startFrom), new Timestamp(endAt)).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }




  def clazzesByClazzDef(id: UUID) = SecuredAction.async { implicit request =>
    cService.clazzesByClazzDef(id).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }


}
