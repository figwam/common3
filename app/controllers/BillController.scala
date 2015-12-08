package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models._
import models.daos._
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class BillController @Inject()(
                                   val messagesApi: MessagesApi,
                                   val env: Environment[User, JWTAuthenticator],
                                   billDAO: BillDAO)
  extends Silhouette[User, JWTAuthenticator] {

  def listBySubscriptionId(idSubscription: UUID) = SecuredAction.async { implicit request =>
    billDAO.listBySubscriptionId(idSubscription).flatMap { ret =>
      Future.successful(Ok(Json.toJson(ret)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem retrieve subscription for "+idSubscription)
        InternalServerError(ex.getMessage)
      case ex: NoSuchElementException => NotFound
      case t: Throwable =>
        Logger.error("Problem retrieve subscription for "+idSubscription)
        BadRequest
      case _ => BadRequest
    }
  }
}
