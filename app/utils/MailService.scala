package utils

import javax.inject.Inject
import com.google.inject.ImplementedBy
import com.sendgrid._
import play.Play
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer._
import scala.concurrent.duration._

@ImplementedBy(classOf[MailServiceDefault])
trait MailService {
  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String): Unit
}

class MailServiceDefault @Inject() (mailerClient: MailerClient) extends MailService {

  lazy val from = Play.application().configuration().getString("play.mailer.from")

  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) =
    mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml)))
}

class MailServiceSendgrid extends MailService {

  lazy val from = Play.application().configuration().getString("sendgrid.from")
  lazy val sgKey = Play.application().configuration().getString("sendgrid.key")
  lazy val mock: Boolean = Play.application().configuration().getString("sendgrid.mock").toBoolean

  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) = {
    val sg = new SendGrid(sgKey)
    val email = new SendGrid.Email()
    //email.setTemplateId("abc123-def456");
    email.addTo(recipients.toArray)
    email.setFrom(from)
    email.setSubject(subject)
    email.setHtml(bodyHtml)
    if (!mock) sg.send(email) else Logger.debug("The confirmation email:"+email.getHtml)
  }
}