package models

import java.util.UUID

import play.api.libs.json._


case class Registration(
                         id: Option[UUID],
                         idTrainee: UUID,
                         idClazz: UUID)


/**
 * The companion object.
 */
object Registration {

  /**
   * Converts the [Clazz] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Registration]
}