package models.daos

import java.sql.Timestamp
import java.util._
import javax.inject.Inject

import models._
import play.Play
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import utils.Utils._

trait ClazzDefinitionDAO extends DAOSlick {

  def create(clazz: ClazzDefinition): Future[ClazzDefinition]
  //  def update(id: Long, clazz: ClazzDefinition): Future[Int]
  //  def delete(id: Long): Future[Int]
  def listActive(): Future[Seq[ClazzDefinition]]
  //  def findById(id: Long): Future[ClazzDefinition]
  def count: Future[Int]

  /**
    * Lists all clazz definitions
    *
    * @param page
    * @param pageSize
    * @param orderBy
    * @param idPartner
    * @return
    */
  def listByPartner(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, idPartner: UUID): Future[PageClazzDefinition]
}

class ClazzDefinitionDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzDefinitionDAO with DAOSlick {
  import driver.api._

  private def count(filter: String): Future[Int] =
    db.run(slickClazzDefinitions.filter(_.name.toLowerCase like filter.toLowerCase).length.result)


  override def count: Future[Int] =
    db.run(slickClazzDefinitions.length.result)


  override def create(clazz: ClazzDefinition): Future[ClazzDefinition] = {
    val insertQuery = slickClazzDefinitions.returning(slickClazzDefinitions.map(_.id)).into((clazzDB, id) => clazzDB.copy(id = id))
    val action = insertQuery += model2entity(clazz)
    db.run(action).map(_ => clazz)
  }


  override def listActive(): Future[Seq[ClazzDefinition]] = {
    val now = new Timestamp(System.currentTimeMillis())
    val query =
      for {
        clazz <- slickClazzDefinitions if clazz.activeFrom <= now if clazz.activeTill >= now
      } yield (clazz)
    val result = db.run(query.result)
    result.map { clazz =>
      clazz.map {
        case (clazz) => entity2model(clazz)
      }
    }
  }

  private def countByPartner(idPartner: UUID): Future[Int] = {
    val action = (for {
      studio <- slickStudios.filter(_.idPartner === idPartner)
      clazzDef <- slickClazzDefinitions.filter(_.idStudio === studio.id)
    } yield ())
    db.run(action.length.result)
  }

  override def listByPartner(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, idPartner: UUID): Future[PageClazzDefinition] = {
    val offset = if (page > 0) pageSize * page else 0

    val action = (for {
      studio <- slickStudios.filter(_.idPartner === idPartner)
      clazzDef <- slickClazzDefinitions.filter(_.idStudio === studio.id)
    } yield (studio, clazzDef)).drop(offset).take(pageSize)
    val totalRows = countByPartner(idPartner)


    val result = db.run(action.result)
    result.map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (studio, clazz) => entity2model(clazz)
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => PageClazzDefinition(c3, page, offset.toLong, rows.toLong)))


  }

}
