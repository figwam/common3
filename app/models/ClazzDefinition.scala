package models

import java.net.URL
import java.sql.Timestamp
import java.util.{Calendar, UUID}
import javax.inject.Inject

import models.Recurrence.Recurrence
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Reads._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.Utils._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Recurrence extends Enumeration {
  type Recurrence = Value
  val onetime, weekly = Value
}


object Type extends Enumeration {
  type Type = Value
  val aerobic, boxen, kickboxen, crossfit, gymnastic, yoga, parcour, mma, thaiboxen, dance = Value
}

case class ClazzDefinition(
                            id: Option[UUID],
                            startFrom: Calendar,
                            endAt: Calendar,
                            activeFrom: Calendar,
                            activeTill: Calendar,
                            recurrence: Recurrence,
                            name: String,
                            contingent: Short,
                            avatarurl: Option[URL] = None,
                            description: String,
                            tags: Option[String],
                            isActive: Boolean,
                            amount: scala.math.BigDecimal,
                            vat: Option[scala.math.BigDecimal],
                            currency: Option[String],
                            idStudio: Option[UUID])


/**
 * The companion object.
 */
object ClazzDefinition {

  import utils.Utils.Implicits._

  /**
   * Converts the [Clazz] object to Json and vice versa.
   */
  implicit val jsonFormat = Json.format[ClazzDefinition]
  implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
      (__ \ 'description).read[String](minLength[String](1))
    )

}

trait ClazzDefinitionService extends DAOSlick {



  def create(clazz: ClazzDefinition): Future[ClazzDefinition]
  def retrieve(id: UUID): Future[Option[ClazzDefinition]]
  def update (clazz: ClazzDefinition): Future[ClazzDefinition]
  def delete(id: UUID): Future[Int]

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

class ClazzDefinitionServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzDefinitionService {

  import driver.api._

  private def count(filter: String): Future[Int] =
    db.run(slickClazzDefinitions.filter(_.name.toLowerCase like filter.toLowerCase).length.result)


  override def count: Future[Int] =
    db.run(slickClazzDefinitions.length.result)


  override def create(obj: ClazzDefinition): Future[ClazzDefinition] = {
    val insertQuery = slickClazzDefinitions.returning(slickClazzDefinitions.map(_.id)).into((objDB, id) => objDB.copy(id = id))
    val action = insertQuery += model2entity(obj)
    db.run(action).map(objDB => entity2model(objDB))
  }

  override def retrieve(id: UUID): Future[Option[ClazzDefinition]] = {
    db.run(slickClazzDefinitions.filter(_.id === id).result.headOption).map(o => o.map(c => entity2model(c)))
  }


  override def update(cl: ClazzDefinition): Future[ClazzDefinition] = {
    val q = for {c <- slickClazzDefinitions if c.id === cl.id} yield
      (c.startFrom, c.endAt, c.activeFrom, c.activeTill,
        c.name, c.recurrence, c.contingent, c.updatedOn,
        c.avatarurl, c.description, c.tags, c.isActive, c.amount)
    db.run(q.update(asTimestamp(cl.startFrom), asTimestamp(cl.endAt), asTimestamp(cl.activeFrom), asTimestamp(cl.activeTill),
      cl.name, cl.recurrence + "", cl.contingent, new Timestamp(System.currentTimeMillis()),
      cl.avatarurl.map(_.toString()), cl.description, Some(cl.tags.getOrElse("") + ""), cl.isActive, cl.amount)).map(_ => cl)
  }


  override def delete(id: UUID): Future[Int] = db.run(slickClazzDefinitions.filter(_.id === id).delete)


  override def listActive(): Future[Seq[ClazzDefinition]] = {
    val now = new Timestamp(System.currentTimeMillis())
    val query =
      for {
        clazz <- slickClazzDefinitions if clazz.activeFrom <= now if clazz.activeTill >= now if clazz.isActive
      } yield clazz
    val result = db.run(query.result)
    result.map { clazz =>
      clazz.map {
        case clazz => entity2model(clazz)
      }
    }
  }

  private def countByPartner(idPartner: UUID): Future[Int] = {
    val action = for {
      studio <- slickStudios.filter(_.idPartner === idPartner)
      clazzDef <- slickClazzDefinitions.filter(_.idStudio === studio.id)
    } yield ()
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
    }.flatMap(c3 => totalRows.map(rows => PageClazzDefinition(c3, page, offset.toLong, rows.toLong)))

  }
}