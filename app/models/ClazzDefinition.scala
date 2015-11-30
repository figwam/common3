package models

import java.util.{Calendar, UUID}

import models.Recurrence.Recurrence
import models.Type.Type

import play.api.libs.json._

object Recurrence extends Enumeration {
  type Recurrence = Value
  val onetime, weekly = Value
}


object Type extends Enumeration {
  type Type = Value
  val aerobic, boxen, kickboxen, crossfit, gymnastic, yoga, parcour, mma, thaiboxen, dance = Value
}

case class ClazzDefinition(
                  id: Option[UUID],
                  startFrom: Calendar,
                  endAt: Calendar,
                  activeFrom: Calendar,
                  activeTill: Calendar,
                  recurrence: Recurrence,
                  name: String,
                  contingent: Short,
                  avatarurl: Option[String] = None,
                  description: String,
                  tags: Option[String],
                  isActive: Boolean,
                  amount: scala.math.BigDecimal,
                  vat: scala.math.BigDecimal,
                  currency: String,
                  idStudio: Option[UUID])


/**
 * The companion object.
 */
object ClazzDefinition {

  import utils.Utils.Implicits._

  /**
   * Converts the [Clazz] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ClazzDefinition]

}