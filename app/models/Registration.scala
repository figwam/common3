package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.daos.DAOSlick
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class Registration(
                         id: Option[UUID],
                         idTrainee: UUID,
                         idClazz: UUID)

object Registration {
  implicit val jsonFormat = Json.format[Registration]
}

trait RegistrationService {

  def countByTrainee(idTrainee:UUID): Future[Int]

  def save(registration: Registration): Future[Registration]

  def delete(idRegistration: UUID): Future[Int]
}

class RegistrationServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends RegistrationService with DAOSlick {

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
