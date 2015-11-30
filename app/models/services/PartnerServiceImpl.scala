package models.services

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.Partner
import models.daos.PartnerDAO
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to partners.
 *
 * @param partnerDAO The partner DAO implementation.
 */
class PartnerServiceImpl @Inject() (partnerDAO: PartnerDAO) extends PartnerService {

  /**
   * Retrieves a partner that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a partner.
   * @return The retrieved partner or None if no partner could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[Partner]] = partnerDAO.find(loginInfo)

  /**
   * Saves a partner.
   *
   * @param partner The partner to save.
   * @return The saved partner.
   */
  def save(partner: Partner) = partnerDAO.save(partner)


  /*
  /**
   * NOTE: We dont need it: this is login with some social provider
   *
   *
   *
   * Saves the social profile for a partner.
   *
   * If a partner exists for this profile then update the partner, otherwise create a new partner with the given profile.
   *
   * @param profile The social profile to save.
   * @return The partner for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    partnerDAO.find(profile.loginInfo).flatMap {
      case Some(partner) => // Update partner with profile
        partnerDAO.save(partner.copy(
          firstname = profile.firstName,
          lastname = profile.lastName,
          fullname = profile.fullName,
          email = profile.email,
          avatarurl = profile.avatarURL
        ))
      case None => // Insert a new partner
        partnerDAO.save(partner(
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
