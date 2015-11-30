package models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json._


case class Offer(    id: Option[UUID],
                     name: String,
                     nrAccess: Short,
                     price: scala.math.BigDecimal,
                     priceTimestop: scala.math.BigDecimal,
                     createdOn:DateTime)


/**
 * The companion object.
 */
object Offer {

  import utils.Utils.Implicits._

  /**
   * Converts the [Offer] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Offer]
}