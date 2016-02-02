package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}

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

  def delete(id: UUID) = SecuredAction.async { implicit request =>
    validateDelete(id, request.identity.id, service.delete)
  }

}
