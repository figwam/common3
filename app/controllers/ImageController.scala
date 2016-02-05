package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.Play
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.{JsSuccess, Json, JsError}
import play.api.libs.ws.WS
import utils.{FormValidator, ImageService}
import scala.concurrent._
import ExecutionContext.Implicits.global
import models._
import play.api.Play.current

/**
 * The basic photos controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class ImageController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                      imageService: ImageService)
  extends Silhouette[User, JWTAuthenticator] {


  lazy val name = Play.application().configuration().getString("cloudinary.name")

  def create = UserAwareAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.Image] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[FormValidator.Image] => {
        request.body.validate[FormValidator.Image].map { obj =>
          val id = UUID.randomUUID()
          //val idp = UUID.randomUUID()+"\\.\\w+".r.findAllIn(obj.file)
          imageService.createAsync(obj.file, id, "gymix, "+request.path)
          Future.successful(Created(Json.obj("message" -> Messages("save.ok")))
            .withHeaders(("Location",request.path+"/"+id+".jpg")))
        }
      }.recoverTotal {
        case error =>
          Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
      }
    }
  }


  def retrieve(id: UUID) = UserAwareAction.async { implicit request =>
    // Make the request
    WS.url("http://res.cloudinary.com/"+name+"/image/upload/"+id+".jpg")
      .withRequestTimeout(10000).getStream().map {
      case (response, body) =>
        // Check that the response was successful
        if (response.status == 200) {
          // Get the content type
          val contentType = response.headers.get("Content-Type").flatMap(_.headOption)
            .getOrElse("application/octet-stream")
          // If there's a content length, send that, otherwise return the body chunked
          response.headers.get("Content-Length") match {
            case Some(Seq(length)) =>
              Ok.feed(body).as(contentType).withHeaders("Content-Length" -> length)
            case _ =>
              Ok.chunked(body).as(contentType)
          }
        } else {
          BadGateway
        }
    }
  }


  def delete(id: UUID) = UserAwareAction.async { implicit request =>
    imageService.deleteAsync(id)
    Future.successful(Ok("OK"))
  }

}
