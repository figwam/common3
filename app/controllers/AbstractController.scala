package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.{AbstractModel, User}
import org.postgresql.util.PSQLException
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.Result

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by alex on 02/02/16.
  */
abstract class AbstractController extends Silhouette[User, JWTAuthenticator]{

  /**
    *
    * @param enrich The List of Tupels which will be enriched to the request json and parsed to model.
    *               A typical value is an ID, which comes from backend and not from client.
    * @param dbAction The action which should be performed on the object (update, create)
    * @param request The http request
    * @param r
    * @param rr
    * @tparam F
    * @tparam T
    * @return
    */
  protected def validateUpsert[F,T](enrich: Option[List[Tuple2[String,String]]], dbAction: T => Future[Any])(implicit request: SecuredRequest[JsValue], r: Reads[F], rr: Reads[T]): Future[Result] = {
    // validate the object against the form, which can differ from internal object model
    // e.g. the client form does not have the ID of owner
    request.body.validate[F] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[F] => {

        // define the doEnrich Function
        def doEnrich (o:JsObject, enrich: List[Tuple2[String,String]]):JsObject = enrich match {
          case Nil => o
          case t :: tail => doEnrich(o + ( t._1.toString -> Json.toJson(t._2.toString)), tail)
        }

        //define the doProcess function
        def doProcess(enrichedJson: JsValue) = {
          enrichedJson.validate[T].map { obj =>
            dbAction(obj).map{
              // execute the action on the object and return the created Response
              case o if o.isInstanceOf[AbstractModel] => {
                  o.asInstanceOf[AbstractModel].id.get
                  Created(Json.obj("message" -> Messages("save.ok")))
                    .withHeaders(("Location", request.path + "/" + o.asInstanceOf[AbstractModel].id.get))
              }
              case o if o.isInstanceOf[Int] && o.asInstanceOf[Int] == 1  =>
                Ok(Json.obj("message" -> Messages("save.ok")))
            }.recover {
              case ex:PSQLException if ex.getMessage.contains("CONTINGENT_EXCEEDED") =>
                logger.error("Updating or Creating Object failed")
                BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> Messages(ex.getMessage)))
            }
          }.recoverTotal {
            case error =>
              Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
          }
        }

        enrich match {
          // if we have values to enrich, do so, NO otherwise
          case Some(o) => doProcess(doEnrich(request.body.as[JsObject],o))
          case None => doProcess(request.body)
        }
      }
    }
  }

  protected def retrieveById[T](id: UUID, dbAction: (UUID) => Future[Option[T]])(implicit r: Reads[T], w: Writes[T]): Future[Result] = {
    dbAction(id).flatMap(o => retrieveResponse(o))
  }

  protected def retrieveByOwner[T](id: UUID, owner: UUID, dbAction: (UUID, UUID) => Future[Option[T]])(implicit r: Reads[T], w: Writes[T]): Future[Result] = {
    dbAction(id, owner).flatMap(o => retrieveResponse(o))
  }

  protected def deleteById[T](id: UUID, dbAction: (UUID) => Future[Int]): Future[Result] = {
    dbAction(id).flatMap(r => r match {
      case 0 => Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found"))))
      case 1 => Future.successful(Ok)
      case _ => Logger.error("WTH?!? We expect NO or exactly one unique result here")
        Future.successful(InternalServerError)
    })
  }

  private def retrieveResponse[T](o: Option[T])(implicit r: Reads[T], w: Writes[T]) =
    o.fold(Future.successful(NotFound(Json.obj("message" -> Messages("object.not.found")))))(c => Future.successful(Ok(Json.toJson(c))))

}
