package models.daos

import java.sql.Timestamp
import javax.inject.Inject


import models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait StudioDAO extends DBTableDefinitions {
  def insert(studio: Studio): Future[Studio]
}

class StudioDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends StudioDAO with DAOSlick {

  import driver.api._

  override def insert(studio: Studio): Future[Studio] = {
    // We don't have the studio id so we try to get it first.
    // If there is no Studio yet for this partner we retrieve the id on insertion.
    val studioAction = {
      val retrieveStudio = slickStudios.filter(
        studio => studio.id === studio.id).result.headOption
      val insertStudio = slickStudios.returning(slickStudios.map(_.id)).
        into((studio, id) => studio.copy(id = id)) += model2entity(studio)
      for {
        studioOption <- retrieveStudio
        studio <- studioOption.map(DBIO.successful(_)).getOrElse(insertStudio)
      } yield studio
    }
    db.run(studioAction).map(_ => studio)
  }
}
