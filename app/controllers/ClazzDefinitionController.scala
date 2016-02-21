package controllers

import java.util.UUID
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
                                       @Named("ClazzScheduler") clazzActor: ActorRef)
  extends AbstractController {


  import utils.FormValidator.ClazzDef._

  def create  = SecuredAction.async(parse.json) { implicit request =>
    // import the ClazzDefeinition CREATE specific Validator,
    // we must have an idStudio for CREATE (but only for CREATE)
    import utils.FormValidator.ClazzDefCreate._
    validateUpsert(None, service.create)
  }



  def retrieve(id: UUID) = SecuredAction.async { implicit request =>
    retrieveById(id, service.retrieve)
  }


  def retrieveOwn(id: UUID) = SecuredAction.async { implicit request =>
    retrieveByOwner(id, request.identity.id.get, service.retrieveByOwner)
  }

  def updateOwn(id: UUID) = SecuredAction.async(parse.json) { implicit request =>
    // call the retrieve by owner function first, if we get a result "Ok", it means the
    // logged in user owns the resource he wants to update, allow update, otherwise "NotFound"
    // will be returned
    retrieveByOwner(id, request.identity.id.get, service.retrieveByOwner).flatMap ( r => r match {
      case Result(h,_,_) if h.status == play.api.http.Status.OK => {
        // add server side ids to the request JSON (logged in user, the id)
        val requestEnrichment = List(("id", id.toString), ("idPartner", request.identity.id.get.toString))
        validateUpsert(Some(requestEnrichment), service.update)
      }
      case _ => Future.successful(r)
    })
  }

  def deleteOwn(id: UUID) = SecuredAction.async { implicit request =>
    // call the retrieve by owner function first, if we get a result "Ok", it means the
    // logged in user owns the resource he wants to delete, allow delete, otherwise "NotFound"
    // will be returned
    retrieveByOwner(id, request.identity.id.get, service.retrieveByOwner).flatMap ( r => r match {
      case Result(h,_,_) if h.status == play.api.http.Status.OK => deleteById(id, service.delete)
      case _ => Future.successful(r)
    })
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

}
