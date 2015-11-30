package models.services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.Trainee

import scala.concurrent.Future

/**
 * Handles actions to trainees.
 */
trait TraineeService extends IdentityService[Trainee] {


  /**
   * Retrieves a trainee that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a trainee.
   * @return The retrieved trainee or None if no trainee could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[Trainee]]
  /**
   * Saves a trainee.
   *
   * @param trainee The trainee to save.
   * @return The saved trainee.
   */
  def save(trainee: Trainee): Future[Trainee]

  /*
  /**
   * NOTE: We dont need it: this is login with some social provider
   *
   * Saves the social profile for a trainee.
   *
   * If a trainee exists for this profile then update the trainee, otherwise create a new trainee with the given profile.
   *
   * @param profile The social profile to save.
   * @return The trainee for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[Trainee]
  */
}
