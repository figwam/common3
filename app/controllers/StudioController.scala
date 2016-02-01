package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.iteratee.Iteratee
import play.api.libs.json._
import play.api.mvc.{Result}
import utils.FormValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */


@Singleton
class StudioController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       service: StudioService)
  extends Silhouette[User, JWTAuthenticator] {


  def create = SecuredAction.async(parse.json) { implicit request =>
    validateUpsert(service.create)
  }


  def retrieve = SecuredAction.async { implicit request =>
    service.retrieve(request.identity.id.get).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))
    }
  }

  def update = SecuredAction.async(parse.json) { implicit request =>
    validateUpsert(service.update).flatMap { _ =>
      Future.successful(Ok(Json.obj("message" -> Messages("save.ok"))))
    }
  }

  def delete = SecuredAction.async { implicit request =>
    service.delete(request.identity.id.get).flatMap { r => r match {
      case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found"))))
      case 1 => Future.successful(Ok)
      case _ => Logger.error("WTH?!? We expect NO or exactly one unique result here")
        Future.successful(InternalServerError);
    }
    }
  }

  def validateUpsert(dbAction: Studio => Future[Studio])(implicit request: SecuredRequest[JsValue]): Future[Result] = {
    request.body.validate[FormValidator.Studio] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.Studio] => {
        request.body.validate[Studio].map { obj =>
          dbAction(obj.copy(idPartner=request.identity.id)).flatMap {
            case o:Studio =>
              Future.successful(Created(Json.obj("message" -> Messages("save.ok")))
                .withHeaders(("Location",request.path+"/"+o.id.get)))
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
}
