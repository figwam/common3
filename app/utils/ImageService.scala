package utils

import java.util
import java.util.UUID

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.inject.ImplementedBy
import play.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._

@ImplementedBy(classOf[ImageServiceCloudinary])
trait ImageService {
  def createAsync(path: String, id: UUID): Unit
  def createImage(path: String, id: UUID): Unit
  def deleteAsync(id: UUID): Unit
  def deleteImage(id: UUID): Unit
}

class ImageServiceCloudinary extends ImageService {

  lazy val name = Play.application().configuration().getString("cloudinary.name")
  lazy val key = Play.application().configuration().getString("cloudinary.key")
  lazy val secret = Play.application().configuration().getString("cloudinary.secret")

  def createAsync(path: String, id: UUID) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      createImage(path, id)
    }
  }

  def createImage(path: String, id: UUID) = {
    val config = new util.HashMap[String,String]
    config.put("cloud_name", name)
    config.put("api_key", key)
    config.put("api_secret", secret)
    val cloudinary = new Cloudinary(config)
    val options = ObjectUtils.asMap(
      "public_id", id.toString,
      "tags", "gymix",
      "format", "jpg"
    )
    cloudinary.uploader().upload(path, options)
  }

  def deleteAsync(id: UUID) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      deleteImage(id)
    }
  }

  def deleteImage(id: UUID) = {
    val config = new util.HashMap[String,String]
    config.put("cloud_name", name)
    config.put("api_key", key)
    config.put("api_secret", secret)
    val cloudinary = new Cloudinary(config)
    cloudinary.uploader().destroy(id.toString, ObjectUtils.emptyMap())
  }

}
