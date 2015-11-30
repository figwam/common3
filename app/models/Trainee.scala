package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json._


case class Trainee(
                    id: Option[UUID],
                    loginInfo: LoginInfo,
                    firstname: Option[String],
                    lastname: Option[String],
                    mobile: Option[String] = None,
                    phone: Option[String] = None,
                    email: Option[String] = None,
                    emailVerified: Boolean = false,
                    createdOn: java.sql.Timestamp,
                    updatedOn: java.sql.Timestamp,
                    ptoken: Option[String] = None,
                    isActive: Boolean = true,
                    inactiveReason: Option[String] = None,
                    username: Option[String] = None,
                    fullname: Option[String] = None,
                    avatarurl: Option[String] = None,
                    address: Address) extends Identity


/**
 * The companion object.
 */
object Trainee {

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  /**
   * Converts the [Trainee] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Trainee]
}