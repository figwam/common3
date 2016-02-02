package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.{DAOSlick, DBTableDefinitions}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class Studio (
                   id: Option[UUID] = None,
                   name: String,
                   mobile: Option[String] = None,
                   phone: Option[String] = None,
                   email: Option[String] = None,
                   avatarurl: Option[String] = None,
                   description: Option[String] = None,
                   sporttype: Option[String] = None,
                   idAddress: Option[UUID] = None,
                   address: Option[Address] = None,
                   idPartner: Option[UUID] = None)

/**
 * The companion object.
 */
object Studio {

  import utils.Utils.Implicits._

  /**
   * Converts the [Partner] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Studio]
}

trait StudioService extends DBTableDefinitions {

  // CRUD
  def create(obj: Studio): Future[Studio]
  def retrieve(owner: UUID): Future[Option[Studio]]
  def update (objIn: Studio): Future[Studio]
  def delete(owner: UUID): Future[Int]

}



/**
  * General implementation of the Studio ressource. It does not check if the address belongs to user or not.
  *
  * @param dbConfigProvider
  */
class StudioServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                  service: AddressService)
  extends StudioService with DAOSlick {

  import driver.api._

  override def create(obj: Studio): Future[Studio] = {
    val actions = (for {
      a <- slickAddresses.returning(slickAddresses.map(_.id)).into((objDB, id) => objDB.copy(id = id)) += model2entity(obj.address.get)
      s <- slickStudios.returning(slickStudios.map(_.id)).into((objDB, id) => objDB.copy(id = id)) += model2entity(obj).copy(idAddress = a.id.get)
    } yield s).transactionally

    // run actions and return studio afterwards
    db.run(actions).map(s => obj.copy(id=s.id))
  }

  override def retrieve(owner: UUID): Future[Option[Studio]] = {
    db.run(slickStudios.filter(_.idPartner === owner).result.headOption).map(obj => obj.map(o => entity2model(o)))
  }

  override def update(objIn: Studio): Future[Studio] = {
    val q = for {
      obj <- slickStudios if obj.idPartner === objIn.idPartner
    } yield
      (obj.name, obj.mobile, obj.phone, obj.email, obj.avatarurl, obj.description, obj.sporttype,
        obj.updatedOn)
    db.run(q.update(objIn.name, objIn.mobile, objIn.phone, objIn.email, objIn.avatarurl, objIn.description, objIn.sporttype,
      new Timestamp(System.currentTimeMillis()))).map(_ => objIn)
  }


  override def delete(owner: UUID): Future[Int] = db.run(slickStudios.filter(_.idPartner === owner).delete)


}