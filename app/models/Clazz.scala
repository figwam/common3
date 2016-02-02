package models

import java.net.URL
import java.sql.Timestamp
import java.util.{Calendar, UUID}
import javax.inject.Inject

import models.{DAOSlick, Page}
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
    * @param orderBy
    * @param filter
    * @param idTrainee
    * @return
    */
  def listPersonalizedAll(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%", idTrainee: UUID): Future[Page]

  /**
    * Returns only classes to which the user is registered (inner join).
    *
    * @param page
    * @param pageSize
    * @param orderBy
    * @param filter
    * @param idTrainee
    * @param startFrom
    * @return
    */
  def listPersonalizedMy(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%", idTrainee: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Page]

  def count: Future[Int]

}

class ClazzServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzService with DAOSlick {
  import driver.api._

  override def create(obj: Clazz): Future[Clazz] = {
    val insertQuery = slickClazzes.returning(slickClazzes.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => obj.copy(id=objDB.id))
  }

  override def delete(id: UUID): Future[Int] = db.run(slickClazzes.filter(_.id === id).delete)


  private def count(filter: String): Future[Int] =
    db.run(slickClazzViews.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).filter(_.searchMeta.toLowerCase like filter.toLowerCase).length.result)

  private def countMy(filter: String, idTrainee: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Int] = {
    val action = for {
      registration <- slickRegistrations.filter(_.idTrainee === idTrainee)
      clazz <- slickClazzes.filter(_.id === registration.idClazz)
      clazzView <- slickClazzViews
        .filter(_.startFrom >= startFrom)
        .filter(_.endAt <= endAt)
        .filter(_.startFrom >= new Timestamp(System.currentTimeMillis()))
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) if clazzView.id === registration.idClazz
    } yield registration
    db.run(action.length.result)
  }


  /**
    * Count clazzes
    */
  override def count: Future[Int] =
    db.run(slickClazzes.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).length.result)


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

    db.run(sorted.drop(offset).take(pageSize).result).map { clazz => //result is Seq[DBClazz]
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, studio, addressS)  => {
          entity2model(clazz, studio, addressS)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }


  override def listPersonalizedAll(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%", idTrainee: UUID): Future[Page] = {
    val offset = if (page > -1) pageSize * page else 0
    /*
    The following query is executed, which returns all personalized clazzes and additionally the reservation id.

        SELECT id, ext_id, start_from, end_at, name, contingent,
          avatarurl, description, tags, search_meta, nr_of_regs, id_clazzdef, id_studio, id_trainee, id_registration
        FROM (SELECT
                c.id AS cid,
                t.id AS id_trainee,
                r.id AS id_registration
              FROM clazz c, registration r, trainee t
              WHERE r.id_trainee = t.id AND r.id_clazz = c.id) a
          RIGHT OUTER JOIN clazz_view b
            ON b.id = a.cid and id_trainee = 4;
     */

    val regAction = (for {
      trainee <- slickTrainees
      reg <- slickRegistrations.filter(_.idTrainee === trainee.id).filter(_.idTrainee === idTrainee)
      clazz1 <- slickClazzes.filter(_.id === reg.idClazz)
    } yield (reg))

    val clazzAction = (for {
      (registration, clazz) <- regAction joinRight slickClazzViews
        .sortBy(r => orderBy match {case 1 => r.startFrom case _ => r.startFrom})
        .filter(_.startFrom >= new Timestamp(System.currentTimeMillis()))
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) on (_.idClazz === _.id)
      s <- slickStudios.filter(_.id === clazz.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    //(clazz, registrations) <- slickClazzViews.sortBy(r => orderBy match {case 1 => r.startFrom case _ => r.startFrom}) joinRight slickRegistrations on (_.id === _.idClazz)
    //if clazz.startFrom >= new Timestamp(System.currentTimeMillis()) if clazz.searchMeta.toLowerCase like filter.toLowerCase
    } yield (clazz, registration, s, a)).drop(offset).take(pageSize)
    val totalRows = count(filter)


    val result = db.run(clazzAction.result)
    result.map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, registration, studio, addressS) => {
          val idReg: Option[UUID] = registration.flatMap{reg => reg match {case DBRegistration(_,_,_,_) => reg.id case _ => None} }
          entity2model(clazz, studio, addressS, idReg)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }

  override def listPersonalizedMy(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%", idTrainee: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Page] = {
    val offset = if (page > 0) pageSize * page else 0

    val action = (for {
      registration <- slickRegistrations.filter(_.idTrainee === idTrainee)
      clazz <- slickClazzes.filter(_.id === registration.idClazz)
      clazzView <- slickClazzViews
        .sortBy(r => orderBy match {case 1 => r.startFrom case _ => r.startFrom})
        .filter(_.startFrom >= startFrom)
        .filter(_.endAt <= endAt)
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) if clazzView.id === registration.idClazz
      s <- slickStudios.filter(_.id === clazzView.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    } yield (clazzView, registration, s, a)).drop(offset).take(pageSize)
    val totalRows = countMy(filter, idTrainee, startFrom, endAt)


    val result = db.run(action.result)
    result.map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, registration, studio, addressS) => {
          entity2model(clazz, studio, addressS, registration.id)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }


}
