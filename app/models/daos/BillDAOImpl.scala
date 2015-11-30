package models.daos

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models._
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import utils.Utils._

import utils.Utils._

trait BillDAO {

  def listBySubscriptionId(idSubscription: UUID): Future[List[Bill]]

}

class BillDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends BillDAO with DAOSlick {

  import driver.api._

  override def listBySubscriptionId(idSubscription: UUID): Future[List[Bill]] = {
    val q = for {
      b <- slickBills.filter(_.idSubscription === idSubscription).sortBy(_.periodStart)
    } yield b

    db.run(q.result).map{ bills =>
      bills.toList.map(b => Bill(b.id, b.amount, asCalendar(b.createdOn), b.vat, asCalendar(b.periodStart), asCalendar(b.periodEnd), b.paidAt match { case Some(c) => Some(asCalendar(c)) case _ => None}))
    }
  }
}
