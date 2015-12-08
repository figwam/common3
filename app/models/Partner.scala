package models

import java.util.{GregorianCalendar, Calendar, UUID}

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json._


case class Partner(
                    override val id: Option[UUID] = None,
                    override val firstname: Option[String] = None,
                    override val lastname: Option[String] = None,
                    override val mobile: Option[String] = None,
                    override val phone: Option[String] = None,
                    override val email: Option[String] = None,
                    override val emailVerified: Option[Boolean] = None,
                    override val createdOn: Option[Calendar] = None,
                    override val updatedOn: Option[Calendar] = None,
                    override val ptoken: Option[String] = None,
                    override val isActive: Option[Boolean] = None,
                    override val inactiveReason: Option[String] = None,
                    override val username: Option[String] = None,
                    override val fullname: Option[String] = None,
                    override val avatarurl: Option[String] = None,
                    override val idAddress: Option[UUID] = None,
                    revenue: Option[BigDecimal] = None) extends User


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