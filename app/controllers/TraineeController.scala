package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{LogoutEvent, Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models._
import play.api.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.Future
import utils.FormValidator
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class TraineeController @Inject()(
                                   val messagesApi: MessagesApi,
                                   val env: Environment[User, JWTAuthenticator],
                                   service: TraineeService)
  extends AbstractController {

  import utils.FormValidator.User._

  def retrieve = SecuredAction.async { implicit request =>
    retrieveById(request.identity.id.get, service.retrieve)
  }

  def update = SecuredAction.async(parse.json) { implicit request =>
    val requestEnrichment = List(("id", request.identity.id.get.toString))
    validateUpsert(Some(requestEnrichment), service.update)
  }

  def delete = SecuredAction.async { implicit request =>
    deleteById(request.identity.id.get, service.delete)
  }
}
