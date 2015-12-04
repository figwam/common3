package controllers

import java.sql.Timestamp
import java.util.concurrent.TimeoutException
import java.util.{GregorianCalendar, UUID}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos._
import play.Play
import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import utils.FormValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class AddressController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[Trainee, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       dao: AddressDAO)
  extends Silhouette[Trainee, JWTAuthenticator] {

  def create() = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.Address] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.Address] => {
        request.body.validate[Address].map { obj =>

          dao.create(obj).flatMap {
            case obj:Address =>
              Future.successful(Created(Json.obj("message" -> Messages("save.ok")))
                .withHeaders(("Location",request.path+"/"+obj.id.get.toString())))
            case _ =>
              logger.error("Updating or Creating Object failed")
              Future.successful(InternalServerError(Json.obj("message" -> Messages("save.fail"))))
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }




  def update(id: UUID) = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.Address] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.Address] => {
        request.body.validate[Address].map { obj =>
          dao.update(obj).flatMap {
            case obj:Address =>
              Future.successful(Ok(Json.obj("message" -> Messages("save.ok"))))
            case _ =>
              logger.error("Updating or Creating Object failed")
              Future.successful(InternalServerError(Json.obj("message" -> Messages("save.fail"))))
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }

  def retrieve(id: UUID) = SecuredAction.async { implicit request =>
    dao.retrieve(id).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("clazzdef.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))
    }
  }

  def delete(id: UUID) = SecuredAction.async { implicit request =>
    dao.delete(id).flatMap { r => r match {
      case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("clazzdef.not.found"))))
      case 1 => Future.successful(Ok(Json.obj("message" -> Messages("clazzdef.not.found"))))
      case _ => Logger.error("WTH?!? Whe expect NO or exactly one unique result here")
        Future.successful(InternalServerError);
    }
    }
  }
}
