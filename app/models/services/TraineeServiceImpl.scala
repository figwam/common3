package models.services

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.Trainee
import models.daos.TraineeDAO
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to trainees.
 *
 * @param traineeDAO The trainee DAO implementation.
 */
class TraineeServiceImpl @Inject() (traineeDAO: TraineeDAO) extends TraineeService {

  /**
   * Retrieves a trainee that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a trainee.
   * @return The retrieved trainee or None if no trainee could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[Trainee]] = traineeDAO.find(loginInfo)

  /**
   * Saves a trainee.
   *
   * @param trainee The trainee to save.
   * @return The saved trainee.
   */
  def save(trainee: Trainee) = traineeDAO.save(trainee)


  /*
  /**
   * NOTE: We dont need it: this is login with some social provider
   *
   *
   *
   * Saves the social profile for a trainee.
   *
   * If a trainee exists for this profile then update the trainee, otherwise create a new trainee with the given profile.
   *
   * @param profile The social profile to save.
   * @return The trainee for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    traineeDAO.find(profile.loginInfo).flatMap {
      case Some(trainee) => // Update trainee with profile
        traineeDAO.save(trainee.copy(
          firstname = profile.firstName,
          lastname = profile.lastName,
          fullname = profile.fullName,
          email = profile.email,
          avatarurl = profile.avatarURL
        ))
      case None => // Insert a new trainee
        traineeDAO.save(Trainee(
          id = None,
          loginInfo = profile.loginInfo,
          extId = UUID.randomUUID(),
          firstname = profile.firstName,
          lastname = profile.lastName,
          mobile = None,
          phone = None,
          email = profile.email,
          emailVerified = false,
          createdOn = new Timestamp(System.currentTimeMillis()),
          updatedOn = new Timestamp(System.currentTimeMillis()),
          ptoken = None,
          isActive = true,
          inactiveReason = None,
          username = profile.email,
          fullname = profile.fullName,
          avatarurl = profile.avatarURL,
          null
        ))
    }
  }
  */

}
