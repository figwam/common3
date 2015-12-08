package controllers

import play.api.Play.current
import play.api.libs.ws.WS
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global


class Callouts extends Controller {

  def getStaticGoogleMap = Action.async {

    // Make the request
    WS.url("http://maps.googleapis.com/maps/api/staticmap")
      .withRequestTimeout(10000)
      .withQueryString("center" -> "Bern")
      .withQueryString("zoom" -> "13")
      .withQueryString("size" -> "600x300")
      .withQueryString("maptype" -> "roadmap")
      .withQueryString("markers" -> "color:red|label:S|46.9481282,7.4465944")
      .withQueryString("markers" -> "color:red|label:G|46.9485384,7.4473883")
      .withQueryString("markers" -> "color:red|label:C|46.931828,7.419808").getStream().map {
        case (response, body) =>

          // Check that the response was successful
          if (response.status == 200) {

            // Get the content type
            val contentType = response.headers.get("Content-Type").flatMap(_.headOption)
              .getOrElse("application/octet-stream")

            // If there's a content length, send that, otherwise return the body chunked
            response.headers.get("Content-Length") match {
              case Some(Seq(length)) =>
                Ok.feed(body).as(contentType).withHeaders("Content-Length" -> length)
              case _ =>
                Ok.chunked(body).as(contentType)
            }
          } else {
            BadGateway
          }
      }
  }

}