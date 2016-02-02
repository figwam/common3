package models

import java.util.UUID
import javax.inject.Inject

import models.DAOSlick
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import utils.Utils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class Offer(    id: Option[UUID],
                     name: String,
                     nrAccess: Short,
                     price: scala.math.BigDecimal,
                     priceTimestop: scala.math.BigDecimal,
                     createdOn:DateTime)


/**
 * The companion object.
 */
object Offer {

  import utils.Utils.Implicits._

  /**
   * Converts the [Offer] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Offer]
}

trait OfferService  {

  def retrieve: Future[List[Offer]]

}

class OfferServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends OfferService with DAOSlick {
  import driver.api._



  override def retrieve: Future[List[Offer]] = {
    db.run(slickOffers.filter(_.isDeleted === false).result)
      .map{ offers =>
        offers.toList.map(offer => Offer(offer.id, offer.name, offer.nrAccess, offer.price, offer.priceTimestop, asDatetime(offer.createdOn)))
      }
  }
}