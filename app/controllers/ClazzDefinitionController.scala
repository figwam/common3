package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import akka.actor.{Props, ActorRef, ActorSystem}
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import play.api.mvc.Result
import workers.ClazzScheduler

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
                                       service: ClazzDefinitionService,
                                       clazzService: ClazzService,
                                       aService: AddressService,
                                       sService: StudioService,
                                       @Named("ClazzScheduler") clazzActor: ActorRef)
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
    // The upset will always return status=created for each success case (update or insert),
    // which is wrong for update. So in case we get 201==Created, we just rewrite the resonse into 200==Ok
    validateUpsert(Some(id), service.update).map{ r =>  r.header.status match {
        case 201 => Ok(Json.obj("message" -> Messages("save.ok")))
      }
    }
  }

  def delete(id: UUID) = SecuredAction.async { implicit request =>
    service.delete(id).flatMap { r => r match {
        case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found"))))
        case 1 => Future.successful(Ok(Json.obj("message" -> Messages("object.not.found"))))
        case _ => Logger.error("WTH?!? Whe expect NO or exactly one unique result here")
          Future.successful(InternalServerError);
      }
    }
  }

  def calculateNextClazzes = SecuredAction.async {
    clazzActor ! "CREATE_CLAZZES"
    Future.successful(Ok("OK"))
  }

  def listByPartner(page: Int, orderBy: Int, filter: String) = SecuredAction.async { implicit request =>
    service.listByPartner(page, 10, orderBy, request.identity.id.getOrElse(UUID.randomUUID())).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }



  protected def validateUpsert(id: Option[UUID], dbAction: ClazzDefinition => Future[ClazzDefinition])(implicit request: SecuredRequest[JsValue]): Future[Result] = {
    request.body.validate[FormValidator.ClazzDef] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.ClazzDef] => {
        request.body.validate[ClazzDefinition].map { obj =>
          sService.retrieveByOwner(request.identity.id.get).flatMap { s => s match {
            case Some(s) =>
              dbAction(obj.copy(id=id, idStudio = s.id)).flatMap {
                case o: ClazzDefinition =>
                  Future.successful(Created(Json.obj("message" -> Messages("save.ok")))
                    .withHeaders(("Location",request.path+"/"+o.id.get)))
                case _ =>
                  logger.error("Updating or Creating Object failed")
                  Future.successful(InternalServerError(Json.obj("message" -> Messages("save.fail"))))
              }
            case None => Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found"))))
            }
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }

}
