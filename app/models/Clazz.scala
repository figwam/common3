package models

import java.net.URL
import java.sql.Timestamp
import java.util.{Calendar, UUID}
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global



case class Clazz(
                  id: Option[UUID],
                  startFrom: Calendar,
                  endAt: Calendar,
                  name: String,
                  contingent: Short,
                  avatarurl: Option[URL],
                  description: String,
                  tags: Option[String],
                  registrations: Short = 0,
                  searchMeta: String,
                  amount: scala.math.BigDecimal,
                  idClazzDef: UUID,
                  idStudio: UUID,
                  idRegistration: Option[UUID])


/**
 * The companion object.
 */
object Clazz {

  import utils.Utils.Implicits._

  /**
   * Converts the [Clazz] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[Clazz]
}

trait ClazzService  {

  def create(obj: Clazz): Future[Clazz]
  def delete(id: UUID): Future[Int]

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page]

  /**
    * Returns all classes (outer join) with some personilized informations like: is trainee registered to class
    *
    * @param page
    * @param pageSize
    * @param sortBy
    * @param filter
    * @param idUser
    * @return
    */
  def listPersonalizedAll(page: Int = 0, pageSize: Int = 10, sortBy: Int = 1, filter: String = "%", idUser: UUID): Future[Page]

  /**
    * Returns only classes to which the user is registered (inner join).
    *
    * @param page
    * @param pageSize
    * @param sortBy
    * @param filter
    * @param idUser
    * @param startFrom
    * @return
    */
  def listPersonalizedMy(page: Int = 0, pageSize: Int = 10, sortBy: Int = 1, filter: String = "%", idUser: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Page]

  def clazzesByClazzDef(idClazzDef: UUID): Future[Page]
  def count: Future[Int]
  def count(filter: String): Future[Int]

}

abstract class ClazzServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzService with DAOSlick {
  import driver.api._

  override def create(obj: Clazz): Future[Clazz] = {
    val insertQuery = slickClazzes.returning(slickClazzes.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => obj.copy(id=objDB.id))
  }

  override def delete(id: UUID): Future[Int] = db.run(slickClazzes.filter(_.id === id).delete)

  /**
    * Count clazzes
    */
  override def count: Future[Int] =
    db.run(slickClazzes.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).length.result)

  override def count(filter: String): Future[Int] =
    db.run(slickClazzViews.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).filter(_.searchMeta.toLowerCase like filter.toLowerCase).length.result)

  def clazzesByClazzDef(idClazzDef: UUID): Future[Page] = {
    val clazzAction = for {
      c <- slickClazzViews.filter(_.idClazzDef === idClazzDef)
      s <- slickStudios.filter(_.id === c.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    } yield (c, s, a)

    db.run(clazzAction.result).map { clazz => //result is Seq[DBClazz]
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, studio, addressS)  => {
          entity2model(clazz, studio, addressS)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => Future.successful(Page(c3, 0, 0L, 0L)))
  }

  override def list(page: Int = 0, pageSize: Int = 10, sortBy: Int = 1, filter: String = "%"): Future[Page] = {
    val offset = if (page > 0) pageSize * page else 0

    val clazzAction = for {
      c <- slickClazzViews
        .filter(_.startFrom >= new Timestamp(System.currentTimeMillis()))
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase)
      s <- slickStudios.filter(_.id === c.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    } yield (c, s, a)

    def sorted = sortBy match {
      case 1 => clazzAction.sortBy( r => r._1.startFrom)
      case -1 => clazzAction.sortBy( r => r._1.startFrom.desc)
      case 2 => clazzAction.sortBy( r => r._1.name)
      case -2 => clazzAction.sortBy( r => r._1.name.desc)
      case _ => clazzAction.sortBy( r => r._1.startFrom)
    }

    val totalRows = count(filter)

    db.run(sorted.drop(offset).take(pageSize).result).map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, studio, addressS)  => {
          entity2model(clazz, studio, addressS)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }


}
