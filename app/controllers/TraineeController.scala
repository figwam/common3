package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{LogoutEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.Future
import utils.FormValidator
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class TraineeController @Inject()(
                                   val messagesApi: MessagesApi,
                                   val env: Environment[User, JWTAuthenticator],
                                   service: TraineeService)
  extends Silhouette[User, JWTAuthenticator] {

  def retrieve = SecuredAction.async { implicit request =>
    service.retrieve(request.identity.id.get).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("trainee.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))
    }
  }

  def update = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.User] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.User] => {
        request.body.validate[Trainee].map { obj =>
          service.update(obj.copy(id=Some(request.identity.id.get))).flatMap {
            case obj:Trainee =>
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

  def delete = SecuredAction.async { implicit request =>
    service.delete(request.identity.id.get).flatMap { r => r match {
        case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("trainee.not.found"))))
        case 1 => {
          env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
          env.authenticatorService.discard(request.authenticator, Ok)
        }
        case _ => Logger.error("WTH?!? We expect NO or exactly one unique result here")
          Future.successful(InternalServerError);
      }
    }
  }

}
