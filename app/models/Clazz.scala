package models

import java.net.URL
import java.util.{GregorianCalendar, Calendar, UUID}

import play.api.libs.json._


case class Clazz(
                  id: Option[UUID],
                  startFrom: Calendar,
                  endAt: Calendar,
                  name: String,
                  contingent: Short,
                  avatarurl: Option[URL],
                  description: String,
                  tags: Option[String],
                  registrations: Short = 0,
                  searchMeta: String,
                  idClazzDef: UUID,
                  idStudio: UUID,
                  idRegistration: Option[UUID],
                  studio: Option[Studio] = None)


/**
 * The companion object.
 */
object Clazz {

  import utils.Utils.Implicits._

  /**
   * Converts the [Clazz] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Clazz]
}