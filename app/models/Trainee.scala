package models

import java.util.{Calendar, UUID}

import play.api.libs.json._


case class Trainee(
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
                    override val idAddress: Option[UUID] = None) extends User


/**
 * The companion object.
 */
object Trainee {
  import utils.Utils.Implicits._

  /**
   * Converts the [Trainee] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Trainee]
}