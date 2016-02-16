package modules

import com.google.inject.AbstractModule
import models._
import models._
import net.codingwell.scalaguice.ScalaModule
import utils._

/**
 * Created by alex on 28/09/15.
 */
class ServiceModule extends AbstractModule with ScalaModule {



  /**
   * Configures the module.
   */
  def configure() {
    bind[TraineeService].to[TraineeServiceImpl]
    bind[PartnerService].to[PartnerServiceImpl]
    bind[ClazzDefinitionService].to[ClazzDefinitionServiceImpl]
    bind[OfferService].to[OfferServiceImpl]
    bind[StudioService].to[StudioServiceImpl]
    bind[RegistrationService].to[RegistrationServiceImpl]
    bind[LoggerService].to[LoggerServiceImpl]
    bind[MailTokenService[MailTokenUser]].to[MailTokenServiceImpl]
    bind[MailService].to[MailServiceSendgrid]
    bind[ImageService].to[ImageServiceCloudinary]
  }
}
