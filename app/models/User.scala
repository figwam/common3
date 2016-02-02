package models

import java.util.{Calendar, GregorianCalendar, UUID}

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.Json

import scala.concurrent.Future


trait User extends Identity with Model {
    override val id: Option[UUID] = None
    val firstname: Option[String] = None
    val lastname: Option[String] = None
    val mobile: Option[String] = None
    val phone: Option[String] = None
    val email: Option[String] = None
    val emailVerified: Option[Boolean] = None
    val createdOn: Option[Calendar]  = None
    val updatedOn: Option[Calendar]  = None
    val ptoken: Option[String] = None
    val isActive: Option[Boolean] = None
    val inactiveReason: Option[String] = None
    val username: Option[String] = None
    val fullname: Option[String] = None
    val avatarurl: Option[String] = None
    val idAddress: Option[UUID] = None
}

trait UserService extends IdentityService[User] {
  def retrieve(loginInfo: LoginInfo): Future[Option[User]]
  def signUp(user: User, loginInfo: LoginInfo, address: Address): Future[User]
}
