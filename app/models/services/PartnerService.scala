package models.services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.Partner

import scala.concurrent.Future

/**
 * Handles actions to partners.
 */
trait PartnerService extends IdentityService[Partner] {


  /**
   * Retrieves a partner that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a partner.
   * @return The retrieved partner or None if no partner could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[Partner]]
  /**
   * Saves a partner.
   *
   * @param partner The partner to save.
   * @return The saved partner.
   */
  def save(partner: Partner): Future[Partner]

  /*
  /**
   * NOTE: We dont need it: this is login with some social provider
   *
   * Saves the social profile for a partner.
   *
   * If a partner exists for this profile then update the partner, otherwise create a new partner with the given profile.
   *
   * @param profile The social profile to save.
   * @return The partner for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[Trainee]
  */
}
