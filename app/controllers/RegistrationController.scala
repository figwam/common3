package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.i18n.{MessagesApi}
import play.api.libs.json.{Json}
import play.api.mvc.Result

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
class RegistrationController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       service: RegistrationService)
  extends AbstractController {

  import utils.FormValidator.Registration._

  def count = SecuredAction.async { implicit request =>
    service.count(request.identity.id.get).flatMap{ count =>
      Future.successful(Ok(Json.toJson(count)))
    }
  }

  def create = SecuredAction.async(parse.json) { implicit request =>
    val requestEnrichment = List(("idTrainee", request.identity.id.get.toString))
    validateUpsert(Some(requestEnrichment),service.create)
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

}
