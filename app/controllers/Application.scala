package controllers

import javax.inject.Inject

import org.jooq.{SQLDialect, DSLContext}
import org.jooq.impl.DSL
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Result, Action, Controller}
import generated.Tables._
import generated.tables.records._


class Application @Inject() (val db:Database,val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def queryAll = Action { request =>

    db.withConnection { connection =>

      val sql:DSLContext = DSL.using(connection,SQLDialect.POSTGRES_9_4)

      val users = sql.selectFrom[UserRecord](USER).fetch()

      Ok(users.toString)

    }
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

  def authenticate  = Action { implicit request =>
    Ok
/*    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.login(formWithErrors))
      login =>

    )*/
/*    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.login(formWithErrors)),
      login =>
        db.withConnection { connection =>
          val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
          val users = Option(sql
            .selectFrom[UserRecord](USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(crypto.sign(login._2)))
            .fetchOne())
          user.map { u =>
            Ok(s"Hello ${u.getFirstname}")
          } getOrElse {
            BadRequest(
              views.html.login(
                loginForm.withGlobalError("Wrong username or password")
              )
            )
          }
        }
    )*/
  }






  def index = Action {

    Ok(views.html.index("Your new application is ready."))

  }

  def noob = Action {
    Ok("ha,noob man!")
  }


}