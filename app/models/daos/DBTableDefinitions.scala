package models.daos

import java.net.URL
import java.sql.Timestamp
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import play.Play
import play.api.libs.json._
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf
import utils.Utils._

/*
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}
*/

case class Page(items: Seq[Clazz], page: Int, offset: Long, total: Long){
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class PageClazzDefinition(items: Seq[ClazzDefinition], page: Int, offset: Long, total: Long){
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


object PageClazzDefinition {implicit val jsonFormat = Json.format[PageClazzDefinition]}

object Page {
  /*
  implicit def searchResultsReads[T](implicit fmt: Reads[T]): Reads[Page[T]] = new Reads[Page[T]] {
    def reads(json: JsValue): Page[T] = new Page[T] (
      (json \ "items") match {
        case JsArray(ts) => ts.map(t => fromJson(t)(fmt))
        case _ => throw new RuntimeException("Elements MUST be a list")
      },
      (json \ "page").as[Int],
      (json \ "offset").as[Long],
      (json \ "total").as[Long]
    )
  }

  implicit def searchResultsWrites[T](implicit fmt: Writes[T]): Writes[Page[T]] = new Writes[Page[T]] {
    def writes(ts: Page[T]) = JsObject(Seq(
      "page" -> JsNumber(ts.page),
      "offset" -> JsNumber(ts.offset),
      "total" -> JsNumber(ts.total),
      "items" -> JsArray(ts.items.map(toJson(_)))
    ))
  }
  */
/*
  implicit def fmt[T](implicit fmt: Format[T]): Format[Page[T]] = new Format[Page[T]] {
    def reads(json: JsValue): Page[T] = new Page[T] (
      (json \ "items") match {
        case JsArray(ts) => ts.map(t => fromJson(t)(fmt))
        case _ => throw new RuntimeException("Elements MUST be a list")
      },
      (json \ "page").as[Int],
      (json \ "offset").as[Long],
      (json \ "total").as[Long]
    )

    def writes(ts: Page[T]) = JsObject(Seq(
      "page" -> JsNumber(ts.page),
      "offset" -> JsNumber(ts.offset),
      "total" -> JsNumber(ts.total),
      "items" -> JsArray(ts.items.map(toJson(_)))
    ))
  }
*/
  implicit val jsonFormat = Json.format[Page]
}


trait DBTableDefinitions {

  protected val driver: JdbcProfile
  import driver.api._

  case class DBAddress(
                        id: Option[UUID],
                        street: String,
                        zip: String,
                        city: String,
                        state: String,
                        country: String,
                        createdOn: java.sql.Timestamp,
                        updatedOn: java.sql.Timestamp,
                        isDeleted: Boolean = false,
                        longitude: Option[scala.math.BigDecimal] = None,
                        latitude: Option[scala.math.BigDecimal] = None
                        )

  class Addresses(_tableTag: Tag) extends Table[DBAddress](_tableTag, "address") {
    def * = (id, street, zip, city, state, country, createdOn, updatedOn, isDeleted, longitude, latitude) <> (DBAddress.tupled, DBAddress.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val street: Rep[String] = column[String]("street")
    val zip: Rep[String] = column[String]("zip")
    val city: Rep[String] = column[String]("city")
    val state: Rep[String] = column[String]("state")
    val country: Rep[String] = column[String]("country")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
    val longitude: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("longitude", O.Default(None))
    val latitude: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("latitude", O.Default(None))
  }

  case class DBBill(
    id: Option[UUID],
    amount: scala.math.BigDecimal,
    createdOn: java.sql.Timestamp,
    vat: scala.math.BigDecimal,
    periodStart: java.sql.Timestamp,
    periodEnd: java.sql.Timestamp,
    paidAt: Option[java.sql.Timestamp],
    paymentTransactionId: Option[String],
    paymentStatus: Option[String],
    idSubscription: UUID
    )


  class Bills(_tableTag: Tag) extends Table[DBBill](_tableTag, "bill") {
    def * = (id, amount, createdOn, vat, periodStart, periodEnd, paidAt, paymentTransactionId, paymentStatus, idSubscription) <> (DBBill.tupled, DBBill.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val amount: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("amount")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val vat: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("vat")
    val periodStart: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("period_start")
    val periodEnd: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("period_end")
    val paidAt: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("paid_at")
    val paymentTransactionId: Rep[Option[String]] = column[Option[String]]("payment_transaction_id")
    val paymentStatus: Rep[Option[String]] = column[Option[String]]("payment_status")
    val idSubscription: Rep[UUID] = column[UUID]("id_subscription")
    lazy val subscriptionFk = foreignKey("subscription_fk", idSubscription, slickSubscriptions)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
  }



  case class DBTransaction(
                     id: Option[UUID],
                     amount: scala.math.BigDecimal,
                     createdOn: java.sql.Timestamp,
                     vat: scala.math.BigDecimal,
                     paidAt: Option[java.sql.Timestamp],
                     paymentTransactionId: Option[String],
                     paymentStatus: Option[String],
                     idTrainee: UUID,
                     idClazz: UUID
                   )


  class Transactions(_tableTag: Tag) extends Table[DBTransaction](_tableTag, "transaction") {
    def * = (id, amount, createdOn, vat, paidAt, paymentTransactionId, paymentStatus, idTrainee, idClazz) <> (DBTransaction.tupled, DBTransaction.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val amount: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("amount")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val vat: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("vat")
    val paidAt: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("paid_at")
    val paymentTransactionId: Rep[Option[String]] = column[Option[String]]("payment_transaction_id")
    val paymentStatus: Rep[Option[String]] = column[Option[String]]("payment_status")
    val idTrainee: Rep[UUID] = column[UUID]("id_subscription")
    val idClazz: Rep[UUID] = column[UUID]("id_subscription")
    lazy val subscriptionFk = foreignKey("trainee_fk", idTrainee, slickSubscriptions)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
    lazy val clazzesFk = foreignKey("clazz_fk", idTrainee, slickClazzes)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
  }


  case class DBClazzDefinition(
                      id: Option[UUID],
                      startFrom: java.sql.Timestamp,
                      endAt: java.sql.Timestamp,
                      activeFrom: java.sql.Timestamp,
                      activeTill: java.sql.Timestamp,
                      name: String,
                      recurrence: String,
                      contingent: Short,
                      createdOn: java.sql.Timestamp,
                      updatedOn: java.sql.Timestamp,
                      avatarurl: Option[String] = None,
                      description: String,
                      tags: Option[String],
                      deletedOn: Option[java.sql.Timestamp] = None,
                      idStudio: UUID,
                      isActive: Boolean = true,
                      amount: scala.math.BigDecimal,
                      vat: scala.math.BigDecimal,
                      currency: String
                      )


  class ClazzDefinitions(_tableTag: Tag) extends Table[DBClazzDefinition](_tableTag, "clazz_definition") {
    def * = (id, startFrom, endAt, activeFrom, activeTill, name, recurrence, contingent, createdOn, updatedOn, avatarurl,description, tags, deletedOn, idStudio, isActive, amount, vat, currency) <>(DBClazzDefinition.tupled, DBClazzDefinition.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val startFrom: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_from")
    val endAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("end_at")
    val activeFrom: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("active_from")
    val activeTill: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("active_till")
    val name: Rep[String] = column[String]("name")
    val recurrence: Rep[String] = column[String]("recurrence", O.Default("ONETIME"))
    val contingent: Rep[Short] = column[Short]("contingent")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    val description: Rep[String] = column[String]("description")
    val tags: Rep[Option[String]] = column[Option[String]]("tags", O.Default(None))
    val deletedOn: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("deleted_on")
    val idStudio: Rep[UUID] = column[UUID]("id_studio")
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
    val amount: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("amount")
    val vat: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("vat")
    val currency: Rep[String] = column[String]("currency")
    lazy val studioFk = foreignKey("studio_fk", idStudio, slickStudios)(r => r.id.get, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Restrict)
  }

  case class DBClazz(
                      id: Option[UUID],
                      startFrom: java.sql.Timestamp,
                      endAt: java.sql.Timestamp,
                      createdOn: java.sql.Timestamp,
                      updatedOn: java.sql.Timestamp,
                      idClazzDef: UUID
                      )


  class Clazzes(_tableTag: Tag) extends Table[DBClazz](_tableTag, "clazz") {
    def * = (id, startFrom, endAt, createdOn, updatedOn, idClazzDef) <>(DBClazz.tupled, DBClazz.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val startFrom: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_from")
    val endAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("end_at")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val idClazzDef: Rep[UUID] = column[UUID]("id_clazzdef")
    lazy val studioFk = foreignKey("studio_fk", idClazzDef, slickStudios)(r => r.id.get, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Restrict)
  }

  case class DBClazzView(
                      id: Option[UUID],
                      startFrom: java.sql.Timestamp,
                      endAt: java.sql.Timestamp,
                      name: String,
                      contingent: Short,
                      avatarurl: Option[String] = None,
                      description: String,
                      tags: Option[String],
                      searchMeta: String,
                      registrations: Short,
                      amount: scala.math.BigDecimal,
                      idClazzDef: UUID,
                      idStudio: UUID
                      )

  class ClazzViews(_tableTag: Tag) extends Table[DBClazzView](_tableTag, "clazz_view") {
    def * = (id, startFrom, endAt, name, contingent, avatarurl,description, tags, searchMeta, registrations, amount, idClazzDef, idStudio) <>(DBClazzView.tupled, DBClazzView.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val startFrom: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_from")
    val endAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("end_at")
    val name: Rep[String] = column[String]("name")
    val contingent: Rep[Short] = column[Short]("contingent")
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    val description: Rep[String] = column[String]("description")
    val tags: Rep[Option[String]] = column[Option[String]]("tags", O.Default(None))
    val searchMeta: Rep[String] = column[String]("search_meta")
    val registrations: Rep[Short] = column[Short]("nr_of_regs")
    val amount: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("amount")
    val idClazzDef: Rep[UUID] = column[UUID]("id_clazzdef")
    val idStudio: Rep[UUID] = column[UUID]("id_studio")
  }


  case class DBClazzTraineeView(
                          id: Option[UUID],
                          startFrom: java.sql.Timestamp,
                          endAt: java.sql.Timestamp,
                          name: String,
                          contingent: Short,
                          avatarurl: Option[String] = None,
                          description: String,
                          tags: Option[String],
                          searchMeta: String,
                          registrations: Short,
                          idClazzDef: UUID,
                          idStudio: UUID,
                          idTrainee: Option[UUID],
                          idRegistration: Option[UUID]
                          )

  class ClazzTraineeViews(_tableTag: Tag) extends Table[DBClazzTraineeView](_tableTag, "clazz_trainee_view") {
    def * = (id, startFrom, endAt, name, contingent, avatarurl,description, tags, searchMeta, registrations, idClazzDef, idStudio, idTrainee, idRegistration) <>(DBClazzTraineeView.tupled, DBClazzTraineeView.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val startFrom: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("start_from")
    val endAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("end_at")
    val name: Rep[String] = column[String]("name")
    val contingent: Rep[Short] = column[Short]("contingent")
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    val description: Rep[String] = column[String]("description")
    val tags: Rep[Option[String]] = column[Option[String]]("tags", O.Default(None))
    val searchMeta: Rep[String] = column[String]("search_meta")
    val registrations: Rep[Short] = column[Short]("nr_of_regs")
    val idClazzDef: Rep[UUID] = column[UUID]("id_clazzdef")
    val idStudio: Rep[UUID] = column[UUID]("id_studio")
    val idTrainee: Rep[Option[UUID]] = column[Option[UUID]]("id_trainee")
    val idRegistration: Rep[Option[UUID]] = column[Option[UUID]]("id_registration")
  }

  case class DBLoginInfo(
    id: Option[UUID],
    providerId: String,
    providerKey: String,
    lastUsed: java.sql.Timestamp,
    expiration: java.sql.Timestamp,
    fingerprint: Option[String] = None,
    createdOn: java.sql.Timestamp,
    idUserDeleted: Option[UUID])


  class LoginInfos(_tableTag: Tag) extends Table[DBLoginInfo](_tableTag, "login_info") {
    def * = (id, providerId, providerKey, lastUsed, expiration, fingerprint, createdOn, idUserDeleted) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val providerId: Rep[String] = column[String]("provider_id")
    val providerKey: Rep[String] = column[String]("provider_key")
    val lastUsed: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("last_used")
    val expiration: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("expiration")
    val fingerprint: Rep[Option[String]] = column[Option[String]]("fingerprint", O.Default(None))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val idUserDeleted: Rep[Option[UUID]] = column[Option[UUID]]("id_user_deleted")
    val index1 = index("idx_login_info_provider_key", (providerId, providerKey))
    val index2 = index("idx_id_trainee_deleted", (idUserDeleted))
  }


  case class DBOAuth1Info(
    id: Option[UUID],
    token: String,
    secret: String,
    idLoginInfo: UUID,
    createdOn: java.sql.Timestamp)


  class OAuth1Infos(_tableTag: Tag) extends Table[DBOAuth1Info](_tableTag, "oauth1_info") {
    def * = (id, token, secret, idLoginInfo, createdOn) <> (DBOAuth1Info.tupled, DBOAuth1Info.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val token: Rep[String] = column[String]("token")
    val secret: Rep[String] = column[String]("secret")
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("oauth1_info_trainee_li_uq", idLoginInfo, unique=true)
  }

  case class DBOAuth2Info(
    id: Option[UUID],
    idLoginInfo: UUID,
    accessToken: String,
    tokenType: Option[String] = None,
    expiresIn: Option[Int] = None,
    refreshToken: Option[String] = None,
    createdOn: java.sql.Timestamp
                           )


  class OAuth2Infos(_tableTag: Tag) extends Table[DBOAuth2Info](_tableTag, "oauth2_info") {
    def * = (id, idLoginInfo, accessToken, tokenType, expiresIn, refreshToken, createdOn) <> (DBOAuth2Info.tupled, DBOAuth2Info.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    val accessToken: Rep[String] = column[String]("access_token")
    val tokenType: Rep[Option[String]] = column[Option[String]]("token_type", O.Default(None))
    val expiresIn: Rep[Option[Int]] = column[Option[Int]]("expires_in", O.Default(None))
    val refreshToken: Rep[Option[String]] = column[Option[String]]("refresh_token", O.Default(None))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("oauth2_info_trainee_li_uq", idLoginInfo, unique=true)
  }

  case class DBOffer(
    id: Option[UUID],
    createdOn: java.sql.Timestamp,
    updatedOn: java.sql.Timestamp,
    name: String, nrAccess: Short,
    price: scala.math.BigDecimal,
    priceTimestop: scala.math.BigDecimal,
    isDeleted: Boolean = false
    )


  class Offers(_tableTag: Tag) extends Table[DBOffer](_tableTag, "offer") {
    def * = (id, createdOn, updatedOn, name, nrAccess, price, priceTimestop, isDeleted) <> (DBOffer.tupled, DBOffer.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val name: Rep[String] = column[String]("name")
    val nrAccess: Rep[Short] = column[Short]("nr_access")
    val price: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("price")
    val priceTimestop: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("price_timestop")
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
  }

  case class DBOpenIDAttribute(id: String,
                               key: String,
                               value: String)


  class OpenIDAttributes(_tableTag: Tag) extends Table[DBOpenIDAttribute](_tableTag, "openidattributes") {
    def * = (id, key, value) <> (DBOpenIDAttribute.tupled, DBOpenIDAttribute.unapply)
    val id: Rep[String] = column[String]("id")
    val key: Rep[String] = column[String]("key")
    val value: Rep[String] = column[String]("value")
  }

  case class DBOpenIDInfo(
                                 id: String,
                                 idLoginInfo: UUID
                                 )


  class OpenIDInfos(_tableTag: Tag) extends Table[DBOpenIDInfo](_tableTag, "openidinfo") {
    def * = (id, idLoginInfo) <> (DBOpenIDInfo.tupled, DBOpenIDInfo.unapply)
    val id: Rep[String] = column[String]("id", O.AutoInc, O.PrimaryKey)
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("password_info_trainee_li_uq", idLoginInfo, unique=true)
  }

  case class DBRegistration(
    id: Option[UUID],
    createdOn: java.sql.Timestamp,
    idTrainee: UUID,
    idClazz: UUID
    )

  class Registrations(_tableTag: Tag) extends Table[DBRegistration](_tableTag, "registration") {
    def * = (id, createdOn, idTrainee, idClazz) <> (DBRegistration.tupled, DBRegistration.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val idTrainee: Rep[UUID] = column[UUID]("id_trainee")
    val idClazz: Rep[UUID] = column[UUID]("id_clazz")
    lazy val clazzFk = foreignKey("clazz_fk", idClazz, slickClazzes)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    lazy val traineeFk = foreignKey("trainee_fk", idTrainee, slickTrainees)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index2 = index("registration_uq", idClazz, unique=true)
  }

  case class DBStudio(
      id: Option[UUID],
      name: String,
      mobile: Option[String] = None,
      phone: Option[String] = None,
      email: Option[String] = None,
      avatarurl: Option[String] = None,
      description: Option[String] = None,
      sporttype: Option[String] = None,
      createdOn: java.sql.Timestamp,
      updatedOn: java.sql.Timestamp,
      isDeleted: Boolean = false,
      deletedReason: Option[String] = None,
      idAddress: UUID,
      idPartner: UUID
    )


  class Studios(_tableTag: Tag) extends Table[DBStudio](_tableTag, "studio") {
    def * = (id, name, mobile, phone, email, avatarurl, description, sporttype, createdOn, updatedOn, isDeleted, deletedReason, idAddress, idPartner) <> (DBStudio.tupled, DBStudio.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val name: Rep[String] = column[String]("name")
    val mobile: Rep[Option[String]] = column[Option[String]]("mobile", O.Default(None))
    val phone: Rep[Option[String]] = column[Option[String]]("phone", O.Default(None))
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    val sporttype: Rep[Option[String]] = column[Option[String]]("sporttype", O.Default(None))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on", O.Default(new Timestamp(System.currentTimeMillis)))
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on", O.Default(new Timestamp(System.currentTimeMillis)))
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
    val deletedReason: Rep[Option[String]] = column[Option[String]]("deleted_reason", O.Default(None))
    val idAddress: Rep[UUID] = column[UUID]("id_address")
    val idPartner: Rep[UUID] = column[UUID]("id_partner")
    lazy val addressFk = foreignKey("address_fk", idAddress, slickAddresses)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    lazy val partnerFk = foreignKey("partner_fk", idPartner, slickTrainees)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
    val index2 = index("studio_uq", idAddress, unique=true)
  }

  case class DBSubscription(
    id: Option[UUID],
    createdOn: java.sql.Timestamp,
    updatedOn: java.sql.Timestamp,
    canceledOn: Option[java.sql.Timestamp] = None,
    deletedOn: Option[java.sql.Timestamp] = None,
    idOffer: UUID,
    idTrainee: UUID
    )


  class Subscriptions(_tableTag: Tag) extends Table[DBSubscription](_tableTag, "subscription") {
    def * = (id, createdOn, updatedOn, canceledOn, deletedOn, idOffer, idTrainee) <> (DBSubscription.tupled, DBSubscription.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val canceledOn: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("canceled_on", O.Default(None))
    val deletedOn: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("deleted_on", O.Default(None))
    val idOffer: Rep[UUID] = column[UUID]("id_offer")
    val idTrainee: Rep[UUID] = column[UUID]("id_trainee")
    lazy val offerFk = foreignKey("offer_fk", idOffer, slickOffers)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    lazy val traineeFk = foreignKey("trainee_fk", idTrainee, slickTrainees)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index2 = index("subscription_uq", idTrainee, unique=true)
    val index3 = index("subscription_uq1", idOffer, unique=true)
  }

  case class DBTimeStop(
    id: Option[UUID],
    stopOn: java.sql.Timestamp,
    reason: String,
    createdOn: java.sql.Timestamp,
    idSubscription: UUID
    )


  class TimeStops(_tableTag: Tag) extends Table[DBTimeStop](_tableTag, "time_stop") {
    def * = (id, stopOn, reason, createdOn, idSubscription) <> (DBTimeStop.tupled, DBTimeStop.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val stopOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("stop_on")
    val reason: Rep[String] = column[String]("reason")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val idSubscription: Rep[UUID] = column[UUID]("id_subscription")
    lazy val subscriptionFk = foreignKey("subscription_fk", idSubscription, slickSubscriptions)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
  }

  case class DBTrainee(
    id: Option[UUID] = None,
    firstname: Option[String] = None,
    lastname: Option[String] = None,
    mobile: Option[String] = None,
    phone: Option[String] = None,
    email: Option[String] = None,
    emailVerified: Boolean = false,
    createdOn: java.sql.Timestamp,
    updatedOn: java.sql.Timestamp,
    ptoken: Option[String] = None,
    isDeleted: Boolean = false,
    deleteReason: Option[String] = None,
    isActive: Boolean = true,
    inactiveReason: Option[String] = None,
    idAddress: UUID,
    username: Option[String] = None,
    fullname: Option[String] = None,
    avatarurl: Option[String] = None
    )


  class Trainees(_tableTag: Tag) extends Table[DBTrainee](_tableTag, "trainee") {
    def * = (id, firstname, lastname, mobile, phone, email, emailVerified, createdOn, updatedOn, ptoken, isDeleted, deleteReason, isActive, inactiveReason, idAddress, username, fullname, avatarurl) <> (DBTrainee.tupled, DBTrainee.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val firstname: Rep[Option[String]] = column[Option[String]]("firstname")
    val lastname: Rep[Option[String]] = column[Option[String]]("lastname")
    val mobile: Rep[Option[String]] = column[Option[String]]("mobile", O.Default(None))
    val phone: Rep[Option[String]] = column[Option[String]]("phone", O.Default(None))
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    val emailVerified: Rep[Boolean] = column[Boolean]("email_verified", O.Default(false))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val ptoken: Rep[Option[String]] = column[Option[String]]("ptoken", O.Default(None))
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
    val deleteReason: Rep[Option[String]] = column[Option[String]]("delete_reason", O.Default(None))
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
    val inactiveReason: Rep[Option[String]] = column[Option[String]]("inactive_reason", O.Default(None))
    val idAddress: Rep[UUID] = column[UUID]("id_address")
    val username: Rep[Option[String]] = column[Option[String]]("username", O.Default(None))
    val fullname: Rep[Option[String]] = column[Option[String]]("fullname", O.Default(None))
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    lazy val addressFk = foreignKey("address_fk", idAddress, slickAddresses)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("trainee_uq", idAddress, unique=true)
    val index2 = index("trainee_username_idx", username)
  }

  case class DBTraineeLoginInfo(
    createdOn: java.sql.Timestamp,
    idTrainee: UUID,
    idLoginInfo: UUID
    )

  class TraineeLoginInfos(_tableTag: Tag) extends Table[DBTraineeLoginInfo](_tableTag, "trainee_login_info") {
    def * = (createdOn, idTrainee, idLoginInfo) <> (DBTraineeLoginInfo.tupled, DBTraineeLoginInfo.unapply)
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val idTrainee: Rep[UUID] = column[UUID]("id_trainee")
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    lazy val traineeFk = foreignKey("trainee_fk", idTrainee, slickTrainees)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("trainee_login_info_trainee_li_uq", idLoginInfo, unique=true)
    val index2 = index("trainee_login_info_trainee_uq", idTrainee, unique=true)
  }



  case class DBTraineePasswordInfo(
                             id: Option[UUID],
                             idLoginInfo: UUID,
                             hasher: String,
                             password: String,
                             salt: Option[String] = None,
                             createdOn: java.sql.Timestamp
                             )


  class TraineePasswordInfos(_tableTag: Tag) extends Table[DBTraineePasswordInfo](_tableTag, "trainee_password_info") {
    def * = (id, idLoginInfo, hasher, password, salt, createdOn) <> (DBTraineePasswordInfo.tupled, DBTraineePasswordInfo.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    val hasher: Rep[String] = column[String]("hasher")
    val password: Rep[String] = column[String]("password")
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Default(None))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("trainee_password_info_trainee_li_uq", idLoginInfo, unique=true)
  }


  //----------------


  case class DBPartner(
                        id: Option[UUID],
                        firstname: Option[String],
                        lastname: Option[String],
                        mobile: Option[String] = None,
                        phone: Option[String] = None,
                        email: Option[String] = None,
                        emailVerified: Boolean = false,
                        createdOn: java.sql.Timestamp,
                        updatedOn: java.sql.Timestamp,
                        ptoken: Option[String] = None,
                        isDeleted: Boolean = false,
                        deleteReason: Option[String] = None,
                        isActive: Boolean = true,
                        inactiveReason: Option[String] = None,
                        idAddress: UUID,
                        username: Option[String] = None,
                        fullname: Option[String] = None,
                        avatarurl: Option[String] = None,
                        revenue: scala.math.BigDecimal
                        )


  class Partners(_tableTag: Tag) extends Table[DBPartner](_tableTag, "partner") {
    def * = (id, firstname, lastname, mobile, phone, email, emailVerified, createdOn, updatedOn, ptoken, isDeleted, deleteReason, isActive, inactiveReason, idAddress, username, fullname, avatarurl, revenue) <> (DBPartner.tupled, DBPartner.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val firstname: Rep[Option[String]] = column[Option[String]]("firstname")
    val lastname: Rep[Option[String]] = column[Option[String]]("lastname")
    val mobile: Rep[Option[String]] = column[Option[String]]("mobile", O.Default(None))
    val phone: Rep[Option[String]] = column[Option[String]]("phone", O.Default(None))
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Default(None))
    val emailVerified: Rep[Boolean] = column[Boolean]("email_verified", O.Default(false))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val updatedOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_on")
    val ptoken: Rep[Option[String]] = column[Option[String]]("ptoken", O.Default(None))
    val isDeleted: Rep[Boolean] = column[Boolean]("is_deleted", O.Default(false))
    val deleteReason: Rep[Option[String]] = column[Option[String]]("delete_reason", O.Default(None))
    val isActive: Rep[Boolean] = column[Boolean]("is_active", O.Default(true))
    val inactiveReason: Rep[Option[String]] = column[Option[String]]("inactive_reason", O.Default(None))
    val idAddress: Rep[UUID] = column[UUID]("id_address")
    val username: Rep[Option[String]] = column[Option[String]]("username", O.Default(None))
    val fullname: Rep[Option[String]] = column[Option[String]]("fullname", O.Default(None))
    val avatarurl: Rep[Option[String]] = column[Option[String]]("avatarurl", O.Default(None))
    val revenue: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("revenue")
    lazy val addressFk = foreignKey("address_fk", idAddress, slickAddresses)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("partner_uq", idAddress, unique=true)
    val index2 = index("partner_username_idx", username)
  }

  case class DBPartnerLoginInfo(
                                 createdOn: java.sql.Timestamp,
                                 idPartner: UUID,
                                 idLoginInfo: UUID
                                 )

  class PartnerLoginInfos(_tableTag: Tag) extends Table[DBPartnerLoginInfo](_tableTag, "partner_login_info") {
    def * = (createdOn, idPartner, idLoginInfo) <> (DBPartnerLoginInfo.tupled, DBPartnerLoginInfo.unapply)
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    val idPartner: Rep[UUID] = column[UUID]("id_partner")
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    lazy val partnerFk = foreignKey("partner_fk", idPartner, slickPartners)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("partner_login_info_partner_li_uq", idLoginInfo, unique=true)
    val index2 = index("partner_login_info_partner_uq", idPartner, unique=true)
  }



  case class DBPartnerPasswordInfo(
                                    id: Option[UUID],
                                    idLoginInfo: UUID,
                                    hasher: String,
                                    password: String,
                                    salt: Option[String] = None,
                                    createdOn: java.sql.Timestamp
                                    )


  class PartnerPasswordInfos(_tableTag: Tag) extends Table[DBPartnerPasswordInfo](_tableTag, "partner_password_info") {
    def * = (id, idLoginInfo, hasher, password, salt, createdOn) <> (DBPartnerPasswordInfo.tupled, DBPartnerPasswordInfo.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val idLoginInfo: Rep[UUID] = column[UUID]("id_login_info")
    val hasher: Rep[String] = column[String]("hasher")
    val password: Rep[String] = column[String]("password")
    val salt: Rep[Option[String]] = column[Option[String]]("salt", O.Default(None))
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
    lazy val loginInfoFk = foreignKey("login_info_fk", idLoginInfo, slickLoginInfos)(r => r.id.get, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
    val index1 = index("partner_password_info_partner_li_uq", idLoginInfo, unique=true)
  }

  case class DBLogger(
                        id: Option[UUID],
                        rootid: String,
                        title: String,
                        exception: String,
                        stacktrace: String,
                        req_header: String,
                        req_method: String,
                        req_address: String,
                        req_uri: String,
                        createdOn: java.sql.Timestamp
                        )

  class Loggers(_tableTag: Tag) extends Table[DBLogger](_tableTag, "logger") {
    def * = (id, rootid, title, exception, stacktrace, reqHeader, reqMethod, reqAddress, reqUri, createdOn) <> (DBLogger.tupled, DBLogger.unapply)
    val id: Rep[Option[UUID]] = column[Option[UUID]]("id", O.PrimaryKey, O.AutoInc)
    val rootid: Rep[String] = column[String]("rootid")
    val title: Rep[String] = column[String]("title")
    val exception: Rep[String] = column[String]("exception")
    val stacktrace: Rep[String] = column[String]("stacktrace")
    val reqHeader: Rep[String] = column[String]("req_header")
    val reqMethod: Rep[String] = column[String]("req_method")
    val reqAddress: Rep[String] = column[String]("req_address")
    val reqUri: Rep[String] = column[String]("req_uri")
    val createdOn: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_on")
  }

  // table query definitions
  val slickTrainees = TableQuery[Trainees]
  val slickPartners = TableQuery[Partners]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickTraineeLoginInfos = TableQuery[TraineeLoginInfos]
  val slickPartnerPasswordInfos = TableQuery[PartnerPasswordInfos]
  val slickPartnerLoginInfos = TableQuery[PartnerLoginInfos]
  val slickTraineePasswordInfos = TableQuery[TraineePasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickOpenIDInfos = TableQuery[OpenIDInfos]
  val slickOpenIDAttributes = TableQuery[OpenIDAttributes]
  val slickClazzes = TableQuery[Clazzes]
  val slickClazzViews = TableQuery[ClazzViews]
  val slickClazzTraineeViews = TableQuery[ClazzTraineeViews]
  val slickClazzDefinitions = TableQuery[ClazzDefinitions]
  val slickOffers = TableQuery[Offers]
  val slickRegistrations = TableQuery[Registrations]
  val slickStudios = TableQuery[Studios]
  val slickSubscriptions = TableQuery[Subscriptions]
  val slickTimeStops = TableQuery[TimeStops]
  val slickAddresses = TableQuery[Addresses]
  val slickLoggers = TableQuery[Loggers]
  val slickBills = TableQuery[Bills]
  val slickTransactions = TableQuery[Transactions]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerId === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)


  /***
    *    88888888888                                 .d888                                        888    d8b
    *        888                                    d88P"                                         888    Y8P
    *        888                                    888                                           888
    *        888  888d888 8888b.  88888b.  .d8888b  888888 .d88b.  888d888 88888b.d88b.   8888b.  888888 888  .d88b.  88888b.  .d8888b
    *        888  888P"      "88b 888 "88b 88K      888   d88""88b 888P"   888 "888 "88b     "88b 888    888 d88""88b 888 "88b 88K
    *        888  888    .d888888 888  888 "Y8888b. 888   888  888 888     888  888  888 .d888888 888    888 888  888 888  888 "Y8888b.
    *        888  888    888  888 888  888      X88 888   Y88..88P 888     888  888  888 888  888 Y88b.  888 Y88..88P 888  888      X88
    *        888  888    "Y888888 888  888  88888P' 888    "Y88P"  888     888  888  888 "Y888888  "Y888 888  "Y88P"  888  888  88888P'
    *
    *
    *
    */
  val vat = BigDecimal(Play.application().configuration().getDouble("vat"))
  val currency = Play.application().configuration().getString("currency")

  def model2entity(address: Address): DBAddress =  DBAddress( address.id, address.street, address.zip, address.city, address.state, address.country, new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), false, address.longitude, address.latitude)
  def entity2model(address: DBAddress): Address = Address( address.id, address.street, address.city, address.zip, address.state, address.country, address.longitude, address.latitude)

  def model2entity(loginInfo: LoginInfo): DBLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey, new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), None, new Timestamp(System.currentTimeMillis), None)

  def model2entity(studio: Studio): DBStudio = DBStudio(id = studio.id, name = studio.name, createdOn = new Timestamp(System.currentTimeMillis), updatedOn = new Timestamp(System.currentTimeMillis), idAddress =  studio.idAddress.getOrElse(UUID.randomUUID()), idPartner =  studio.idPartner.getOrElse(UUID.randomUUID()))
  def entity2model(studio: DBStudio): Studio = Studio(id = studio.id, name = studio.name, idAddress = Some(studio.idAddress), idPartner = Some(studio.idPartner))

  def model2entity(clazz: ClazzDefinition): DBClazzDefinition = DBClazzDefinition(id=clazz.id, startFrom=asTimestamp(clazz.startFrom), endAt=asTimestamp(clazz.endAt), activeFrom=asTimestamp(clazz.activeFrom), activeTill=asTimestamp(clazz.activeTill), name=clazz.name, recurrence=clazz.recurrence+"", contingent=clazz.contingent, createdOn=new Timestamp(System.currentTimeMillis), updatedOn=new Timestamp(System.currentTimeMillis),avatarurl=clazz.avatarurl.map(_.toString),description=clazz.description,tags=clazz.tags, idStudio=clazz.idStudio.getOrElse(UUID.randomUUID()), isActive=clazz.isActive, amount=clazz.amount, vat=clazz.vat.getOrElse(vat*(clazz.amount)), currency=clazz.currency.getOrElse(currency))
  def entity2model(clazz: DBClazzDefinition): ClazzDefinition = ClazzDefinition(clazz.id, asCalendar(clazz.startFrom), asCalendar(clazz.endAt), asCalendar(clazz.activeFrom), asCalendar(clazz.activeTill), Recurrence.withName(clazz.recurrence), clazz.name, clazz.contingent, clazz.avatarurl.map(new URL(_)), clazz.description, clazz.tags, clazz.isActive, clazz.amount, Some(clazz.vat), Some(clazz.currency), Some(clazz.idStudio))

  def model2entity(partner: Partner): DBPartner = DBPartner( partner.id, partner.firstname, partner.lastname, partner.mobile, partner.phone, partner.email, false, new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), partner.ptoken, false, None, true, partner.inactiveReason, UUID.randomUUID(), partner.username, partner.fullname, partner.avatarurl, partner.revenue.getOrElse(BigDecimal(0)))
  def entity2model(partner: DBPartner): Partner = Partner(partner.id, partner.firstname, partner.lastname, partner.mobile, partner.phone, partner.email, Some(partner.emailVerified), Some(asCalendar(partner.createdOn)), Some(asCalendar(partner.updatedOn)), partner.ptoken, Some(partner.isActive), partner.inactiveReason, partner.username, partner.fullname, partner.avatarurl, Some(partner.idAddress), Some(partner.revenue))

  def model2entity(o: Trainee): DBTrainee = DBTrainee( o.id, o.firstname, o.lastname, o.mobile, o.phone, o.email, false, new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), o.ptoken, false, None, true, o.inactiveReason, UUID.randomUUID(), o.username, o.fullname, o.avatarurl)
  def entity2model(o: DBTrainee): Trainee = Trainee(o.id, o.firstname, o.lastname, o.mobile, o.phone, o.email, Some(o.emailVerified), Some(asCalendar(o.createdOn)), Some(asCalendar(o.updatedOn)), o.ptoken, Some(o.isActive), o.inactiveReason, o.username, o.fullname, o.avatarurl, Some(o.idAddress))

  def model2entity(clazz: Clazz): DBClazz = DBClazz(None, asTimestamp(clazz.startFrom), asTimestamp(clazz.endAt), new Timestamp(System.currentTimeMillis), new Timestamp(System.currentTimeMillis), clazz.idClazzDef)
  def entity2model(clazz: DBClazzView, studio: DBStudio, addressStudio: DBAddress, idReg: Option[UUID] = None): Clazz = Clazz(clazz.id, asCalendar(clazz.startFrom), asCalendar(clazz.endAt), clazz.name, clazz.contingent, clazz.avatarurl.map(new URL(_)), clazz.description, clazz.tags, clazz.registrations, clazz.searchMeta, clazz.amount, clazz.idClazzDef, clazz.idStudio, idReg)


}