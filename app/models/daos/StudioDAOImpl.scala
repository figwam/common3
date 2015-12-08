package models.daos

import java.net.URL
import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject


import models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait StudioDAO extends DBTableDefinitions {
  def create(obj: Studio): Future[Studio]
  def retrieve(id: UUID): Future[Option[Studio]]
  //def update (objIn: Studio): Future[Studio]
  def delete(id: UUID): Future[Int]
}

class StudioDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends StudioDAO with DAOSlick {

  import driver.api._

  import driver.api._

  override def create(obj: Studio): Future[Studio] = {
    val insertQuery = slickStudios.returning(slickStudios.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => entity2model(objDB))
  }

  override def retrieve(id: UUID): Future[Option[Studio]] = {
    db.run(slickStudios.filter(_.id === id).result.headOption).map(obj => obj.map(o => entity2model(o)))
  }


  /*override def update(objIn: Studio): Future[Studio] = {
    val q = for {obj <- slickStudios if obj.id === objIn.id} yield
      (obj.id, obj.name, obj.mobile, obj.phone, obj.email, obj.avatarurl, obj.description, obj.sporttype, obj.updatedOn)
    db.run(q.update(objIn.id, objIn.name, objIn.mobile, objIn.phone, objIn.email, objIn.avatarurl.toString, objIn.description, objIn.sporttype,
      new Timestamp(System.currentTimeMillis()))).map(_ => objIn)
  }*/


  override def delete(id: UUID): Future[Int] = db.run(slickStudios.filter(_.id === id).delete)
}
