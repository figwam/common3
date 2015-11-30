package utils

import java.io.{StringWriter, PrintWriter}
import javax.inject._

import models.AppLogger
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import akka.actor.{ActorSystem}

import scala.concurrent._

class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router],
    system: ActorSystem
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  def getStackTraceAsString(t: Throwable) = {
    val sw = new StringWriter
    t.printStackTrace(new PrintWriter(sw))
    sw.toString
  }

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    //http://doc.akka.io/docs/akka/snapshot/general/addressing.html
    val loggerActor = system.actorSelection("/user/DBLogAdmin")
    loggerActor ! AppLogger(exception.id,exception.title,exception.toString,getStackTraceAsString(exception.getCause),request.headers.toString(),request.method,request.remoteAddress,request.uri)
    super.onProdServerError(request,exception)
  }

  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden("You're not allowed to access this resource.")
    )
  }
}