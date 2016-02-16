package models

import java.sql.Timestamp
import java.util.{Calendar, UUID}
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


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

trait TraineeService extends UserService with DAOSlick {

  def retrieve(id: UUID): Future[Option[Trainee]]
  def update (objIn: Trainee): Future[Trainee]
  def delete(id: UUID): Future[Int]
}

/**
  * Give access to the trainee object using Slick
  */
class TraineeServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends TraineeService {

  import driver.api._

  /**
    * Finds a trainee by its login info.
    *
    * @param loginInfo The login info of the trainee to find.
    * @return The found trainee or None if no trainee for the given login info could be found.
    */
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbTraineeLoginInfo <- slickTraineeLoginInfos.filter(_.idLoginInfo === dbLoginInfo.id)
      dbTrainee <- slickTrainees.filter(_.id === dbTraineeLoginInfo.idTrainee)
    } yield (dbTrainee, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (trainee, loginInfo) => entity2model(trainee)
      }
    }
  }

  /**
    * Saves a trainee.
    *
    * @param user The user to save.
    * @return The saved trainee.
    */
  override def signUp(user: User, loginInfo: LoginInfo, address: Address): Future[User] = {

    val trainee:Trainee = user match {
      case u: Trainee => u
      case _ => throw new ClassCastException
    }
    val dbTrainee = model2entity(trainee)
    val dbAddress = model2entity(address)
    val dbLoginInfo = model2entity(loginInfo)

    // We don't have the address id so we try to get it first.
    // If there is no Address yet for this trainee we retrieve the id on insertion.
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
    // If there is no LoginInfo yet for this trainee we retrieve the id on insertion.
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
      dbTraineeP <- slickTrainees.returning(slickTrainees.map(_.id)).insertOrUpdate(dbTrainee.copy(idAddress = address.id.get))
      loginInfo <- loginInfoAction
      _ <- slickTraineeLoginInfos += DBTraineeLoginInfo(new Timestamp(System.currentTimeMillis), dbTraineeP.head.get, loginInfo.id.get)
    } yield ()).transactionally
    // run actions and return trainee afterwards
    db.run(actions).map(_ => trainee)
  }



  /**
    * Updates a trainee.
    *
    * @param trainee The trainee to update.
    * @return The updated trainee.
    */
  def update(trainee: Trainee): Future[Trainee] = {
    val q = for {t <- slickTrainees if t.id === trainee.id} yield (
      t.firstname, t.lastname, t.mobile, t.phone, t.emailVerified, t.updatedOn)
    db.run(q.update(trainee.firstname, trainee.lastname, trainee.mobile, trainee.phone, trainee.emailVerified.getOrElse(false), new Timestamp(System.currentTimeMillis))).map(_ => trainee)
  }

  override def retrieve(id: UUID): Future[Option[Trainee]] = {
    db.run(slickTrainees.filter(_.id === id).result.headOption).map { resultOption =>
      resultOption.map {
        case (trainee) => entity2model(trainee)
      }
    }
  }

  override def delete(id: UUID): Future[Int] = {

    //ns <- coffees.filter(_.name.startsWith("ESPRESSO")).map(_.name).result
    //_ <- DBIO.seq(ns.map(n => coffees.filter(_.name === n).delete): _*)
    //t <- (taUser.filter( (z) => z.taUserId === z.taUserId).map(c => c.status).update(STATUS_ACTIVE))

    val q = (for {
      t <- slickTraineeLoginInfos.filter(_.idTrainee === id).map(_.idLoginInfo).result
      _ <- DBIO.seq(t.map(lid => slickLoginInfos.filter(_.id === lid).map(l => l.idUserDeleted).update(Some(id))): _*)
      _ <- DBIO.seq(t.map(lid => slickTraineePasswordInfos.filter(_.idLoginInfo === lid).delete): _*)
      _ <- slickTraineeLoginInfos.filter(_.idTrainee === id).delete
    } yield ()).transactionally

    db.run(q).map(_ => 1)
  }

}