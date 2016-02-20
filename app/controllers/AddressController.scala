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
  extends AbstractController {

  import utils.FormValidator.Address._

  def retrieve(id: UUID) = UserAwareAction.async { implicit request =>
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
        val requestEnrichment = List(("id", id.toString), ("idPartner", request.identity.id.get.toString),("idTrainee", request.identity.id.get.toString))
        validateUpsert(Some(requestEnrichment), service.update)
      }
    })
  }

  def deleteOwn(id: UUID) = SecuredAction.async { implicit request =>
    // call the retrieve by owner function first, if we get a result "Ok", it means the
    // logged in user owns the resource he wants to delete, allow delete, otherwise "NotFound"
    // will be returned
    retrieveByOwner(id, request.identity.id.get, service.retrieveByOwner).flatMap ( r => r match {
      case Result(h,_,_) if h.status == play.api.http.Status.OK => deleteById(id, service.delete)
    })
  }

}
