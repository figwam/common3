package utils

import java.util
import java.util.UUID
import javax.inject.Inject

import com.cloudinary.{Transformation, Cloudinary}
import com.cloudinary.utils.ObjectUtils
import com.google.inject.ImplementedBy
import play.Play
import play.api.Configuration
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._

@ImplementedBy(classOf[ImageServiceCloudinary])
trait ImageService {
  def createAsync(path: String, id: UUID, tags: String): Unit
  def createImage(path: String, id: UUID, tags: String): Unit
  def deleteAsync(id: UUID): Unit
  def deleteImage(id: UUID): Unit
}

class ImageServiceCloudinary @Inject() (conf: Configuration) extends ImageService {

  lazy val name = conf.getString("cloudinary.name").getOrElse("CL_NAME")
  lazy val key = conf.getString("cloudinary.key").getOrElse("NONE")
  lazy val secret = conf.getString("cloudinary.secret").getOrElse("NONE")

  def createAsync(path: String, id: UUID, tags: String) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      createImage(path, id, tags)
    }
  }

  def createImage(path: String, id: UUID, tags: String) = {
    val config = new util.HashMap[String,String]
    config.put("cloud_name", name)
    config.put("api_key", key)
    config.put("api_secret", secret)
    val cloudinary = new Cloudinary(config)
    val options = ObjectUtils.asMap(
      "public_id", id.toString,
      "tags", tags,
      "format", "jpg",
      "transformation", new Transformation().crop("limit").width(400).height(300)
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
