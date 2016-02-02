package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc._
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
class AddressController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       service: AddressService)
  extends Silhouette[User, JWTAuthenticator] {

  def create = SecuredAction.async(parse.json) { implicit request =>
    validateUpsert(None, service.create)
  }


  def retrieve(id: UUID) = SecuredAction.async { implicit request =>
    service.retrieve(id).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))
    }
  }

  def update(id: UUID) = SecuredAction.async(parse.json) { implicit request =>
    validateUpsert(Some(id), service.update)
  }

  def delete(id: UUID) = SecuredAction.async { implicit request =>
    service.delete(id).flatMap { r => r match {
      case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found"))))
      case 1 => Future.successful(Ok)
      case _ => Logger.error("WTH?!? We expect NO or exactly one unique result here")
        Future.successful(InternalServerError);
    }
    }
  }

  def retrieveByOwner(id: UUID)= SecuredAction.async { implicit request =>
    service.retrieveByOwner(id, request.identity.id.get).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))
    }
  }

  def updateByOwner(id: UUID) = SecuredAction.async(parse.json) { implicit request =>
    service.retrieveByOwner(id, request.identity.id.get).flatMap { o =>
      o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found")))))(c => validateUpsert(Some(id), service.update))
    }
  }

  def validateUpsert(id: Option[UUID], dbAction: Address => Future[Address])(implicit request: SecuredRequest[JsValue]): Future[Result] = {
    request.body.validate[FormValidator.Address] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.Address] => {
        request.body.validate[Address].map { obj =>
          dbAction(obj.copy(id=id)).flatMap {
            case o:Address =>
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

}
