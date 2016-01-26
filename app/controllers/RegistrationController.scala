package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos.RegistrationDAO
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json.Json

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
                                       registrationDAO: RegistrationDAO)
  extends Silhouette[User, JWTAuthenticator] {


  def countByTrainee() = SecuredAction.async { implicit request =>
    registrationDAO.countByTrainee(request.identity.id.get).flatMap{ count =>
      Future.successful(Ok(Json.toJson(count)))
    }
  }

  def create() = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "idClazz").asOpt[String].map { idClazz =>
      registrationDAO.save(Registration(None, request.identity.id.get, UUID.fromString(idClazz))).flatMap { ret =>
        Future.successful(Created)
      }.recover {
        case ex: TimeoutException =>
          Logger.error("Problem create registration for clazz "+idClazz, ex)
          InternalServerError(ex.getMessage)
        case t: Throwable =>
          Logger.error("Problem create registration for clazz "+idClazz, t)
          BadRequest
        case _ => BadRequest
      }
    }.getOrElse {
      Future.successful(BadRequest("Missing parameter [idClazz]"))
    }
  }

  def delete(idRegistration: UUID) = SecuredAction.async { implicit request =>
      registrationDAO.delete(idRegistration).flatMap { ret =>
        Future.successful(Ok)
      }.recover {
        case ex: TimeoutException =>
          Logger.error("Problem delete registration for clazz "+idRegistration, ex)
          InternalServerError(ex.getMessage)
        case t: Throwable =>
          Logger.error("Problem delete registration for clazz "+idRegistration, t)
          BadRequest
        case _ => BadRequest
      }
  }

}
