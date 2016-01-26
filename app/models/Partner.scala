package models

import java.util.{Calendar, UUID}
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{LoginInfo}

import java.sql.Timestamp
import models.daos.DAOSlick
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


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

trait PartnerService extends UserService with DAOSlick {

  def retrieve(id: UUID): Future[Option[Partner]]
  def update (objIn: Partner): Future[Partner]
  def delete(id: UUID): Future[Int]
}

/**
  * Give access to the partner object using Slick
  */
class PartnerServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends PartnerService {

  import driver.api._

  /**
    * Finds a partner by its login info.
    *
    * @param loginInfo The login info of the partner to find.
    * @return The found partner or None if no partner for the given login info could be found.
    */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbPartnerLoginInfo <- slickPartnerLoginInfos.filter(_.idLoginInfo === dbLoginInfo.id)
      dbPartner <- slickPartners.filter(_.id === dbPartnerLoginInfo.idPartner)
    } yield (dbPartner, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (partner, loginInfo) => entity2model(partner)
      }
    }
  }

  /**
    * Saves a partner.
    *
    * @param user The user to save.
    * @return The saved partner.
    */
  override def signUp(user: User, loginInfo: LoginInfo, address: Address): Future[User] = {

    val partner:Partner = user match {
      case u: Partner => u
      case _ => throw new ClassCastException
    }
    val dbPartner = model2entity(partner)
    val dbAddress = model2entity(address)
    val dbLoginInfo = model2entity(loginInfo)

    // We don't have the address id so we try to get it first.
    // If there is no Address yet for this partner we retrieve the id on insertion.
    val addressAction = {
      val retrieveAddress = slickAddresses.filter(
        a => a.id === address.id).result.headOption
      val insertAddress = slickAddresses.returning(slickAddresses.map(_.id)).
        into((address, id) => address.copy(id = id)) += dbAddress
      for {
        addressOption <- retrieveAddress
        address <- addressOption.map(DBIO.successful(_)).getOrElse(insertAddress)
      } yield address
    }

    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this partner we retrieve the id on insertion.
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerId === loginInfo.providerID && info.providerKey === loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = id)) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
      } yield loginInfo
    }

    // combine database actions to be run sequentially
    val actions = (for {
      address <- addressAction
      dbPartnerP <- slickPartners.returning(slickPartners.map(_.id)).insertOrUpdate(dbPartner.copy(idAddress = address.id.get))
      loginInfo <- loginInfoAction
      _ <- slickPartnerLoginInfos += DBPartnerLoginInfo(new Timestamp(System.currentTimeMillis), dbPartnerP.head.get, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return partner afterwards
    db.run(actions).map(_ => partner)
  }



  /**
    * Updates a partner.
    *
    * @param partner The partner to update.
    * @return The updated partner.
    */
  def update(partner: Partner): Future[Partner] = {
    val q = for {t <- slickPartners if t.id === partner.id} yield (
      t.firstname, t.lastname, t.mobile, t.phone, t.updatedOn)
    db.run(q.update(partner.firstname, partner.lastname, partner.mobile, partner.phone, new Timestamp(System.currentTimeMillis))).map(_ => partner)
  }

  override def retrieve(id: UUID): Future[Option[Partner]] = {
    db.run(slickPartners.filter(_.id === id).result.headOption).map { resultOption =>
      resultOption.map {
        case (partner) => entity2model(partner)
      }
    }
  }

  override def delete(id: UUID): Future[Int] = {

    //ns <- coffees.filter(_.name.startsWith("ESPRESSO")).map(_.name).result
    //_ <- DBIO.seq(ns.map(n => coffees.filter(_.name === n).delete): _*)
    //t <- (taUser.filter( (z) => z.taUserId === z.taUserId).map(c => c.status).update(STATUS_ACTIVE))

    val q = (for {
      t <- slickPartnerLoginInfos.filter(_.idPartner === id).map(_.idLoginInfo).result
      _ <- DBIO.seq(t.map(lid => slickLoginInfos.filter(_.id === lid).map(l => l.idUserDeleted).update(Some(id))): _*)
      _ <- DBIO.seq(t.map(lid => slickPartnerPasswordInfos.filter(_.idLoginInfo === lid).delete): _*)
      _ <- slickPartnerLoginInfos.filter(_.idPartner === id).delete
    } yield ()).transactionally

    db.run(q).map(_ => 1)
  }
}