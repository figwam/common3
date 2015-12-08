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
class OthersController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       clazzDAO: ClazzDAO,
                                       partnerDAO: PartnerDAO,
                                       offerDAO: OfferDAO)
  extends Silhouette[User, JWTAuthenticator] {




  def enums = UserAwareAction.async { implicit request =>
    lazy val cacheExpire = Play.application().configuration().getString("cache.expire.get.enums").toInt
    val enums:JsValue = Cache.getAs[JsValue]("enums").getOrElse{
      var recurrences = ArrayBuffer[JsValue]()
      for (d <- Recurrence.values) {
        recurrences += Json.obj(
          "id" -> d,
          "name" -> Messages("enum.clazz.recurrence."+d)
        )
      }
      var types = ArrayBuffer[JsValue]()
      for (d <- Type.values) {
        types += Json.obj(
          "id" -> d,
          "name" -> Messages("enum.clazz.type."+d)
        )
      }
      val e = Json.obj("enums" -> Json.obj(
        "clazz" -> Json.obj(
          "recurrences" -> Json.toJson(recurrences),
          "types" -> Json.toJson(types)
        )
      ))
      Cache.set("enums", e, cacheExpire.seconds)
      e
    }
    Future.successful(Ok(enums))
  }


  def offers = UserAwareAction.async { implicit request =>
    lazy val cacheExpire = Play.application().configuration().getString("cache.expire.get.offers").toInt
    val offers:List[Offer] = Cache.getAs[List[Offer]]("offers").getOrElse{
      val offers:List[Offer] = Await.result(offerDAO.list(), 5.seconds)
      Cache.set("offers", offers, cacheExpire.seconds)
      offers
    }
    Future.successful(Ok(Json.toJson(offers)))
  }
}
