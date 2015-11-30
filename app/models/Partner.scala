package models

import java.util.{Calendar, UUID}

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json._


case class Partner(
                    id: Option[UUID],
                    loginInfo: LoginInfo,
                    firstname: Option[String],
                    lastname: Option[String],
                    mobile: Option[String] = None,
                    phone: Option[String] = None,
                    email: Option[String] = None,
                    emailVerified: Boolean = false,
                    createdOn: Calendar,
                    updatedOn: Calendar,
                    ptoken: Option[String] = None,
                    isActive: Boolean = true,
                    inactiveReason: Option[String] = None,
                    username: Option[String] = None,
                    fullname: Option[String] = None,
                    avatarurl: Option[String] = None,
                    revenue: Option[BigDecimal] = None,
                    address: Address,
                    studio: Studio) extends Identity


/**
 * The companion object.
 */
object Partner {


  import utils.Utils.Implicits._

  /**
   * Converts the [Partner] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Partner]
}