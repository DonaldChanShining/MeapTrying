package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import helpers.Database
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import generated.Tables._
import generated.tables.records._
import play.api.libs.Crypto
import scala.concurrent.ExecutionContext.Implicits.global


@Singleton
class Application @Inject() (val db:Database,
                             val messagesApi: MessagesApi,
                             val crypto: Crypto,implicit val system: ActorSystem) extends Controller with I18nSupport{

  def queryAll = Action.async { request =>

    db.query{dsl =>
      val users = dsl.selectFrom[UserRecord](USER).fetch()
      Ok(users.toString)
    }

   /* db.withConnection { connection =>

      val sql:DSLContext = DSL.using(connection,SQLDialect.POSTGRES_9_4)

      val users = sql.selectFrom[UserRecord](USER).fetch()

      Ok(users.toString)

    }*/
  }

  def login = Action { implicit request =>

    Ok(views.html.login(loginForm))

  }

  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> text
    )
  )

  def authenticate  = Action.async { implicit request =>
/*    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.login(formWithErrors))
      login =>

    )*/
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        Future(BadRequest(views.html.login(formWithErrors))),
      login =>
        //db.withConnection { connection =>
        //val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
        db.query{ sql =>
          val users = Option(sql
            .selectFrom[UserRecord](USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(crypto.sign(login._2)))
            .fetchOne())
          users.map { u =>
            Ok(s"Hello ${u.getFirstname}")
          } getOrElse {
            BadRequest(
              views.html.login(
                loginForm.withGlobalError("Wrong username or password")
              )
            )
          }
        }
    )
  }






  def index = Action {

    Ok(views.html.index("Your new application is ready."))

  }

  def noob = Action {
    Ok("ha,noob man!")
  }


}