package models

import java.util.UUID
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class Registration(
                         override val id: Option[UUID],
                         idTrainee: UUID,
                         idClazz: UUID) extends AbstractModel

object Registration {
  implicit val jsonFormat = Json.format[Registration]
}

trait RegistrationService extends DAOSlick with AbstractService[Registration]{
  def count(idTrainee:UUID): Future[Int]
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

  def delete(idRegistration: UUID): Future[Int] =
    db.run(slickRegistrations.filter(_.id === idRegistration).delete)


  def retrieveByOwner(id: UUID, owner: UUID): Future[Option[Registration]] =
    db.run(slickRegistrations.filter(_.id===id).filter(_.idTrainee===owner).result.headOption).map(obj => obj.map(o => entity2model(o)))

  override def retrieve(id: UUID): Future[Option[Registration]] =
    db.run(slickRegistrations.filter(_.id===id).result.headOption).map(obj => obj.map(o => entity2model(o)))

  override def update(objIn: Registration): Future[Int] = {
    val q = for {
      obj <- slickRegistrations if obj.idTrainee === objIn.idTrainee
    } yield (obj.idClazz)
    db.run(q.update(objIn.idClazz))
  }
}
