package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos.{ClazzDAO, OfferDAO, PartnerDAO}
import play.Play
import play.api.Play.current
import play.api.cache.Cache
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{Json, _}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class UserController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       clazzDAO: ClazzDAO,
                                       partnerDAO: PartnerDAO,
                                       offerDAO: OfferDAO)
  extends Silhouette[User, JWTAuthenticator] {

  /**
   * Returns the user.
   *
   * @return The result to display.
   */
  def user = SecuredAction.async { implicit request =>
    request.identity match {
      case p:Partner => Future.successful(Ok(Json.toJson(p)))
      case _ => Future.successful(InternalServerError)
    }
  }

  /**
   * Manages the sign out action.
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, Ok)
  }

}
