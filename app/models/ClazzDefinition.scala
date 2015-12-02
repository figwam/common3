package models

import java.net.URL
import java.util.{Date, Calendar, UUID}

import models.Recurrence.Recurrence
import play.api.libs.json.Reads._

import play.api.libs.json._
import play.api.libs.functional.syntax._

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
                            avatarurl: Option[URL] = None,
                            description: String,
                            tags: Option[String],
                            isActive: Boolean,
                            amount: scala.math.BigDecimal,
                            vat: Option[scala.math.BigDecimal],
                            currency: Option[String],
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
  implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
      (__ \ 'description).read[String](minLength[String](1))
    )

}