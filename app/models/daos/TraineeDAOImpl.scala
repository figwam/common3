package models.daos

import java.sql.Timestamp
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future


import utils.Utils._

/**
  * Give access to the trainee object.
  */
trait TraineeDAO extends UserService with DAOSlick {

  def retrieve(id: UUID): Future[Option[Trainee]]
  def update (objIn: Trainee): Future[Trainee]
  def delete(id: UUID): Future[Int]
}

/**
 * Give access to the trainee object using Slick
 */
class TraineeDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends TraineeDAO {

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
      t.firstname, t.lastname, t.mobile, t.phone, t.updatedOn)
    db.run(q.update(trainee.firstname, trainee.lastname, trainee.mobile, trainee.phone, new Timestamp(System.currentTimeMillis))).map(_ => trainee)
  }

  override def retrieve(id: UUID): Future[Option[Trainee]] = {
    val query = for {
      dbTrainee <- slickTrainees.filter(_.id === id)
      dbTraineeLoginInfo <- slickTraineeLoginInfos.filter(_.idTrainee === dbTrainee.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbTraineeLoginInfo.idLoginInfo)
    } yield (dbTrainee, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map {
        case (trainee, loginInfo) => entity2model(trainee)
      }
    }
  }

  override def delete(id: UUID): Future[Int] = ???
}
