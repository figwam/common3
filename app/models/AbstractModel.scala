package models

import java.util.UUID


trait AbstractModel{
    val id: Option[UUID] = None
}
