package models

import java.util.UUID

import scala.concurrent.Future


trait AbstractService[T] {
    // CRUD
    def create(o: T): Future[T]
    def retrieve(id: UUID): Future[Option[T]]
    def retrieveByOwner(id: UUID, owner: UUID): Future[Option[T]]
    def update (o: T): Future[Int]
    def delete(id: UUID): Future[Int]
}

