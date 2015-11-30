package models.daos

import java.sql.Timestamp
import java.util.{GregorianCalendar, Calendar, UUID}
import javax.inject.Inject

import models.{Offer, ClazzDefinition, Clazz}
import play.Logger
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import utils.Utils._

trait OfferDAO  {

  def list(): Future[List[Offer]]

}

class OfferDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends OfferDAO with DAOSlick {
  import driver.api._



  override def list(): Future[List[Offer]] = {
    db.run(slickOffers.filter(_.isDeleted === false).result)
      .map{ offers =>
        offers.toList.map(offer => Offer(offer.id, offer.name, offer.nrAccess, offer.price, offer.priceTimestop, asDatetime(offer.createdOn)))
      }
  }
}