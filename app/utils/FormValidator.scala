package utils

import play.api.libs.json._
import play.api.libs.functional.syntax._
import Reads._

/**
 * The form which provides additional Form Validation
 */
object FormValidator {

  case class ClazzDef(name: String, description: String)

  object ClazzDef {
    implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
        (__ \ 'description).read[String](minLength[String](1))
      )(ClazzDef.apply _)
  }


  case class Studio(name: String, description: String)

  object Studio {
    implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
        (__ \ 'description).read[String](minLength[String](1))
      )(Studio.apply _)
  }

  case class Address(street: String,
                     city: String,
                     zip: String,
                     state: String)

  object Address {
    implicit val dataReads = (
        (__ \ 'street).read[String](minLength[String](1)) and
        (__ \ 'city).read[String](minLength[String](1)) and
        (__ \ 'zip).read[String](verifying[String](_.matches("\\d{4,4}"))) and
        (__ \ 'state).read[String]
      )(Address.apply _)
  }


  case class User(
                   firstname: String,
                   lastname: String)

  object User {
    implicit val userReads = (
      (__ \ 'firstname).read[String](minLength[String](1)) and
        (__ \ 'lastname).read[String](minLength[String](1))
      )(User.apply _)
  }
}
