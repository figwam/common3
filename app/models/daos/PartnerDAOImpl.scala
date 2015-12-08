package models.daos

import java.sql.Timestamp
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile


import utils.Utils._
/**
  * Give access to the partner object.
  */
trait PartnerDAO extends UserService with DAOSlick

/**
 * Give access to the partner object using Slick
 */
class PartnerDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends PartnerDAO{

  import driver.api._

  /**
   * Finds a partner by its login info.
   *
   * @param loginInfo The login info of the partner to find.
   * @return The found partner or None if no partner for the given login info could be found.
   */
  override def retrieve(loginInfo: LoginInfo): Future[Option[Partner]] = {
    val query = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbPartnerLoginInfo <- slickPartnerLoginInfos.filter(_.idLoginInfo === dbLoginInfo.id)
      dbPartner <- slickPartners.filter(_.id === dbPartnerLoginInfo.idPartner)
      /*dbAddress <- slickAddresses.filter(_.id === dbPartner.idAddress)
      dbStudio <- slickStudios.filter(_.idPartner === dbPartner.id)
      dbAddressS <- slickAddresses.filter(_.id === dbStudio.idAddress)*/
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
   * @param user The partner to save.
   * @return The saved partner.
   */
  override def signUp(user: User, loginInfo: LoginInfo, address: Address): Future[Partner] = {

    val partner:Partner = user match {
      case u: Partner => u
      case _ => throw new ClassCastException
    }
    val dbPartner = model2entity(partner)
    val dbAddress = model2entity(address)
    val dbLoginInfo = model2entity(loginInfo)
    /*val dbAddressStudio = model2entity(addressStudio)
    val dbStudio = model2entity(studio)*/

    // We don't have the address id so we try to get it first.
    // If there is no Address yet for this partner we retrieve the id on insertion.
    val addressAction = {
      val retrieveAddress = slickAddresses.filter(
        address => address.id === address.id).result.headOption
      val insertAddress = slickAddresses.returning(slickAddresses.map(_.id)).
        into((address, id) => address.copy(id = id)) += dbAddress
      for {
        addressOption <- retrieveAddress
        address <- addressOption.map(DBIO.successful(_)).getOrElse(insertAddress)
      } yield address
    }

    /*val addressActionStudio = {
      val retrieveAddress = slickAddresses.filter(
        address => address.id === addressStudio.id).result.headOption
      val insertAddress = slickAddresses.returning(slickAddresses.map(_.id)).
        into((address, id) => address.copy(id = id)) += dbAddressStudio
      for {
        addressOption <- retrieveAddress
        address <- addressOption.map(DBIO.successful(_)).getOrElse(insertAddress)
      } yield address
    }*/

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
      //addressStudio <- addressActionStudio
      dbPartnerP <- slickPartners.returning(slickPartners.map(_.id)).insertOrUpdate(dbPartner.copy(idAddress = address.id.get))
      //studio <- slickStudios.returning(slickStudios.map(_.id)).insertOrUpdate(dbStudio.copy(idAddress = addressStudio.id.get, idPartner = dbPartnerP.head.get))
      loginInfo <- loginInfoAction
      _ <- slickPartnerLoginInfos += DBPartnerLoginInfo(new Timestamp(System.currentTimeMillis), dbPartnerP.head.get, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return partner afterwards
    db.run(actions).map(_ => partner)
  }

}
