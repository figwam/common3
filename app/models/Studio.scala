package models

import java.net.URL
import java.util.UUID

import play.api.libs.json.Json


case class Studio(
                   id: Option[UUID] = None,
                   name: String,
                   mobile: Option[String] = None,
                   phone: Option[String] = None,
                   email: Option[String] = None,
                   avatarurl: Option[URL] = None,
                   description: Option[String] = None,
                   sporttype: Option[String] = None,
                   idAddress: Option[UUID] = None,
                   idPartner: Option[UUID] = None)

/**
 * The companion object.
 */
object Studio {

  import utils.Utils.Implicits._

  /**
   * Converts the [Partner] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Studio]
}