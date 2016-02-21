package utils

import javax.inject.Inject
import com.google.inject.ImplementedBy
import com.sendgrid._
import play.Play
import play.api.{Configuration, Logger}
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

class MailServiceDefault @Inject() (mailerClient: MailerClient, conf: Configuration) extends MailService {

  lazy val from = conf.getString("play.mailer.from").getOrElse("FROM_DEFAULT")

  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) = {
    Akka.system.scheduler.scheduleOnce(100 milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }
  def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) =
    mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml)))
}

class MailServiceSendgrid @Inject() (conf: Configuration)  extends MailService {

  lazy val from = conf.getString("sendgrid.from").getOrElse("FROM_DEFAULT")
  lazy val sgKey = conf.getString("sendgrid.key").getOrElse("NONE")
  lazy val mock: Boolean = conf.getBoolean("sendgrid.mock").getOrElse(true)

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