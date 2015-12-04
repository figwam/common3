package models.daos

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject


import models._
import play.api.db.slick.DatabaseConfigProvider
import utils.Utils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait AddressDAO extends DBTableDefinitions {

  def create(obj: Address): Future[Address]
  def retrieve(id: UUID): Future[Option[Address]]
  def update (objIn: Address): Future[Address]
  def delete(id: UUID): Future[Int]

  def insert(address: Address): Future[Address]

}

class AddressDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends AddressDAO with DAOSlick {

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
        obj.updatedOn, obj.longitude, obj.latitude)
    db.run(q.update(objIn.street, objIn.zip, objIn.city, objIn.state, objIn.country,
      new Timestamp(System.currentTimeMillis()), objIn.longitude, objIn.latitude)).map(_ => objIn)
  }


  override def delete(id: UUID): Future[Int] = db.run(slickAddresses.filter(_.id === id).delete)


  override def insert(address: Address): Future[Address] = {
    // We don't have the address id so we try to get it first.
    // If there is no Address yet for this partner we retrieve the id on insertion.
    val addressAction = {
      val retrieveAddress = slickAddresses.filter(
        address => address.id === address.id).result.headOption
      val insertAddress = slickAddresses.returning(slickAddresses.map(_.id)).
        into((address, id) => address.copy(id = id)) += model2entity(address)
      for {
        addressOption <- retrieveAddress
        address <- addressOption.map(DBIO.successful(_)).getOrElse(insertAddress)
      } yield address
    }
    db.run(addressAction).map(_ => address)
  }

}
