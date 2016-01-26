package modules

import com.google.inject.AbstractModule
import models._
import models.daos._
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
    bind[ClazzDAO].to[ClazzDAOImpl]
    bind[TraineeService].to[TraineeServiceImpl]
    bind[PartnerService].to[PartnerServiceImpl]
    bind[ClazzDefinitionService].to[ClazzDefinitionServiceImpl]
    bind[OfferDAO].to[OfferDAOImpl]
    bind[RegistrationDAO].to[RegistrationDAOImpl]
    bind[LoggerDAO].to[LoggerDAOImpl]
    bind[SubscriptionDAO].to[SubscriptionDAOImpl]
    bind[BillDAO].to[BillDAOImpl]
    bind[StudioDAO].to[StudioDAOImpl]
    bind[MailTokenService[MailTokenUser]].to[MailTokenServiceImpl]
    bind[MailService].to[MailServiceSendgrid]
  }
}
