package models

import java.util.{UUID, Calendar}

import play.api.libs.json._

case class Subscription(
                         id: Option[UUID],
                         createdOn: Calendar,
                         period: Period,
                         canceledOn: Option[Calendar] = None,
                         deletedOn: Option[Calendar] = None,
                         offer: Option[Offer] = None,
                         timestop: Option[List[Timestop]] = None)


/**
 * The companion object.
 */
object Subscription {

  import utils.Utils.Implicits._

  implicit val jsonFormat = Json.format[Subscription]
}
