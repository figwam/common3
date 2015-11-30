package models

import java.util.{UUID, Calendar}

import play.api.libs.json._

case class Period(start: Calendar, end: Calendar)


object Period {

  import utils.Utils.Implicits._

  implicit val jsonFormat = Json.format[Period]
}