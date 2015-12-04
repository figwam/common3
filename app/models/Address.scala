package models

import java.util.UUID

import play.api.libs.json.Json


case class Address(
                    id: Option[UUID],
                    street: String,
                    city: String,
                    zip: String,
                    state: String,
                    country: String,
                    longitude: Option[scala.math.BigDecimal] = None,
                    latitude: Option[scala.math.BigDecimal] = None
                  )

/**
 * The companion object.
 */
object Address {

  /**
   * Converts the [Partner] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Address]
}