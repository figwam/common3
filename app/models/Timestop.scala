package models

import java.util.{UUID, Calendar}

import play.api.libs.json._


case class Timestop(     id: Option[UUID],
                         stopOn: Calendar,
                         reason: String,
                         createdOn: Calendar)


/**
 * The companion object.
 */
object Timestop {


  import utils.Utils.Implicits._
  /**
   * Converts the [Bill] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Timestop]
}