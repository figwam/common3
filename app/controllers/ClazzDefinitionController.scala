package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import utils.FormValidator

/**
 * The basic clazz def controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class ClazzDefinitionController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       dao: ClazzDefinitionDAO)
  extends Silhouette[User, JWTAuthenticator] {


  def create() = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.ClazzDef] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.ClazzDef] => {
        request.body.validate[ClazzDefinition].map { clazzDef =>

          // In case of onetime set the activTill to class endDate, so it will be automatically historized
          val clazzDefCopy = clazzDef.recurrence match {
            case Recurrence.onetime => clazzDef.copy(activeTill = clazzDef.endAt)
          }

          dao.create(clazzDefCopy).flatMap {
            case c:ClazzDefinition =>
              Future.successful(Created(Json.obj("message" -> Messages("save.ok")))
                .withHeaders(("Location",request.path+"/"+c.id.get.toString())))
            case _ =>
              Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"))))
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }




  def update(id: UUID) = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.ClazzDef] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.ClazzDef] => {
        request.body.validate[ClazzDefinition].map { clazzDef =>

          // In case of onetime set the activTill to class endDate, so it will be automatically historized
          val clazzDefCopy = clazzDef.recurrence match {
            case Recurrence.onetime => clazzDef.copy(activeTill = clazzDef.endAt)
          }

          dao.update(clazzDefCopy).flatMap {
            case c:ClazzDefinition =>
              Future.successful(Ok(Json.obj("message" -> Messages("save.ok"))))
            case _ =>
              Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"))))
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

  def listByPartner(page: Int, orderBy: Int, filter: String) = SecuredAction.async { implicit request =>
    dao.listByPartner(page, 10, orderBy, request.identity.id.getOrElse(UUID.randomUUID())).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }

}
