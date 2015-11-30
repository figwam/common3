package models.daos

import java.sql.Timestamp
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future


import utils.Utils.asCalendar

trait RegistrationDAO {

  def countByTrainee(idTrainee:UUID): Future[Int]

  def save(registration: Registration): Future[Registration]

  def delete(idRegistration: UUID): Future[Int]
}

class RegistrationDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends RegistrationDAO with DAOSlick {

  import driver.api._

  def countByTrainee(idTrainee:UUID): Future[Int] = db.run(slickRegistrations.filter(_.idTrainee === idTrainee).length.result)

  def save(registration: Registration): Future[Registration] = {
    db.run(slickRegistrations += DBRegistration(None, new Timestamp(System.currentTimeMillis()), registration.idTrainee, registration.idClazz))
      .map(_ => registration)
  }

  def delete(idRegistration: UUID): Future[Int] = {
    db.run(slickRegistrations.filter(_.id === idRegistration).delete)
  }

}
