package models.daos

import java.sql.Timestamp
import javax.inject.Inject


import models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait AddressDAO extends DBTableDefinitions {

  def update(address: Address): Future[Address]
  def insert(address: Address): Future[Address]

}

class AddressDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends AddressDAO with DAOSlick {

  import driver.api._


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


  override def update(address: Address): Future[Address] = {
    val q = for {a <- slickAddresses if a.id === address.id}yield (a.street, a.city, a.zip, a.updatedOn)
    val updateAction = q.update(address.street, address.city, address.zip, new Timestamp(System.currentTimeMillis))
    db.run(updateAction).map(_ => address)
  }

}
