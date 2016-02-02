package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.{DAOSlick, DBTableDefinitions}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class Address(
                    id: Option[UUID],
                    street: String,
                    city: String,
                    zip: String,
                    state: String,
                    country: String,
                    longitude: Option[scala.math.BigDecimal] = None,
                    latitude: Option[scala.math.BigDecimal] = None
                  )

/**
 * The companion object.
 */
object Address {

  /**
   * Converts the [Partner] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Address]
}

trait AddressService extends DBTableDefinitions {

  // CRUD Addresses in general
  def create(obj: Address): Future[Address]
  def retrieve(id: UUID): Future[Option[Address]]
  def update (objIn: Address): Future[Address]
  def delete(id: UUID): Future[Int]
  def retrieveByOwner(id: UUID, owner: UUID): Future[Option[Address]]

  /*
  def retrieve(id: UUID, uid: UUID): Future[Option[Address]]
  def update(objIn: Address, uid: UUID): Future[Address]
  */

}



/**
  * General implementation of the Address ressource. It does not check if the address belongs to user or not.
  *
  * @param dbConfigProvider
  */
abstract class AddressServiceImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends AddressService with DAOSlick {

  import driver.api._

  override def create(obj: Address): Future[Address] = {
    val insertQuery = slickAddresses.returning(slickAddresses.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => entity2model(objDB))
  }

  override def retrieve(id: UUID): Future[Option[Address]] = {
    db.run(slickAddresses.filter(_.id === id).result.headOption).map(obj => obj.map(o => entity2model(o)))
  }


  override def update(objIn: Address): Future[Address] = {
    val q = for {obj <- slickAddresses if obj.id === objIn.id} yield
      (obj.street, obj.zip, obj.city, obj.state, obj.country,
        obj.updatedOn)
    db.run(q.update(objIn.street, objIn.zip, objIn.city, objIn.state, objIn.country,
      new Timestamp(System.currentTimeMillis()))).map(_ => objIn)
  }


  override def delete(id: UUID): Future[Int] = db.run(slickAddresses.filter(_.id === id).delete)


}
