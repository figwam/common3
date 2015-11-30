package models.daos

import java.sql.Timestamp
import java.util.{GregorianCalendar, Calendar, UUID}
import javax.inject.Inject

import models.{Address, Studio, ClazzDefinition, Clazz}
import play.Logger
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import utils.Utils.{asTimestamp, asCalendar}

trait ClazzDAO  {

  def insert(clazz: Clazz, idClazzDef: UUID): Future[Clazz]
  //  def update(id: Long, clazz: Clazz): Future[Int]
  //  def delete(id: Long): Future[Int]
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

class ClazzDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzDAO with DAOSlick {
  import driver.api._


  private def count(filter: String): Future[Int] =
    db.run(slickClazzViews.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).filter(_.searchMeta.toLowerCase like filter.toLowerCase).length.result)

  private def countMy(filter: String, idTrainee: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Int] = {
    val action = (for {
      registration <- slickRegistrations.filter(_.idTrainee === idTrainee)
      clazz <- slickClazzes.filter(_.id === registration.idClazz)
      clazzView <- slickClazzViews
        .filter(_.startFrom >= startFrom)
        .filter(_.endAt <= endAt)
        .filter(_.startFrom >= new Timestamp(System.currentTimeMillis()))
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) if clazzView.id === registration.idClazz
    } yield (registration))
    db.run(action.length.result)
  }


  /**
   * Count clazzes
   */
  override def count: Future[Int] =
    db.run(slickClazzes.filter(_.startFrom >= new Timestamp(System.currentTimeMillis())).length.result)


  override def insert(clazz: Clazz, idClazzDef: UUID): Future[Clazz] = {
    db.run(slickClazzes += DBClazz(None, asTimestamp(clazz.startFrom), asTimestamp(clazz.endAt), new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), idClazzDef))
      .map(_ => clazz)
  }

  override def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page] = {
    val offset = if (page > 0) pageSize * page else 0
    val clazzAction = (for {
      clazz <- slickClazzViews.sortBy(r => orderBy match {case 1 => r.startFrom case _ => r.startFrom}) if clazz.startFrom >= new Timestamp(System.currentTimeMillis()) if clazz.searchMeta.toLowerCase like filter.toLowerCase
      s <- slickStudios.filter(_.id === clazz.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    } yield (clazz, s, a)).drop(offset).take(pageSize)
    val totalRows = count(filter)

    val result = db.run(clazzAction.result)
    result.map { clazz => //result is Seq[DBClazz]
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, studio, addressS)  => {
          //Logger.info(clazzDef.name+"("+clazz.id+")-->"+registrations._2)
          Clazz(clazz.id, asCalendar(clazz.startFrom), asCalendar(clazz.endAt), clazz.name, clazz.contingent, clazz.avatarurl, clazz.description, clazz.tags, clazz.registrations, clazz.searchMeta, clazz.idClazzDef, clazz.idStudio, None,
            Some(Studio(
              id=studio.id,
              name=studio.name,
              address=Address(
                addressS.id,
                addressS.street,
                addressS.city,
                addressS.zip,
                addressS.state,
                addressS.country)
            )))
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
          Clazz(clazz.id, asCalendar(clazz.startFrom), asCalendar(clazz.endAt), clazz.name, clazz.contingent, clazz.avatarurl, clazz.description, clazz.tags, clazz.registrations, clazz.searchMeta, clazz.idClazzDef, clazz.idStudio, idReg,
            Some(Studio(
              id=studio.id,
              name=studio.name,
              address=Address(
                addressS.id,
                addressS.street,
                addressS.city,
                addressS.zip,
                addressS.state,
                addressS.country)
            )))
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
    } yield (clazzView, registration)).drop(offset).take(pageSize)
    val totalRows = countMy(filter, idTrainee, startFrom, endAt)


    val result = db.run(action.result)
    result.map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, registration) => {
          Clazz(clazz.id, asCalendar(clazz.startFrom), asCalendar(clazz.endAt), clazz.name, clazz.contingent, clazz.avatarurl, clazz.description, clazz.tags, clazz.registrations, clazz.searchMeta, clazz.idClazzDef, clazz.idStudio, registration.id)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }


}
