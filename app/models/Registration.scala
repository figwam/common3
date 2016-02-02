package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.DAOSlick
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class Registration(
                         override val id: Option[UUID],
                         idTrainee: UUID,
                         idClazz: UUID) extends Model

object Registration {
  implicit val jsonFormat = Json.format[Registration]
}

trait RegistrationService extends DAOSlick {
  def count(idTrainee:UUID): Future[Int]
  def create(obj: Registration): Future[Registration]
  def delete(idRegistration: UUID, owner: Option[UUID]): Future[Int]
}

class RegistrationServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends RegistrationService {

  import driver.api._

  def count(idTrainee:UUID): Future[Int] = db.run(slickRegistrations.filter(_.idTrainee === idTrainee).length.result)

  def create(obj: Registration): Future[Registration] = {
    val insertQuery = slickRegistrations.returning(slickRegistrations.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => entity2model(objDB))
  }

  def delete(idRegistration: UUID, owner: Option[UUID]): Future[Int] = owner match {
    case Some(o) => db.run(slickRegistrations.filter(_.id === idRegistration).filter(_.idTrainee===o).delete)
    case None => db.run(slickRegistrations.filter(_.id === idRegistration).delete)
  }


}
