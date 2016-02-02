package models

import java.util.UUID


trait Model{
    val id: Option[UUID] = None
}
