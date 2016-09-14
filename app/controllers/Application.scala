package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import helpers.Database
import play.api.data._
import play.api.cache.CacheApi
import play.api.data.Forms._

import scala.concurrent.Future
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Result, _}
import generated.Tables._
import generated.tables.records._
import play.api.libs.Crypto

import scala.concurrent.ExecutionContext.Implicits.global


class Application @Inject() (val db:Database,
                             val cache: CacheApi,
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
      //      Ok(s"Hello ${u.getFirstname}")
            Redirect(routes.Application.index()).withSession(
              USER.ID.getName -> u.getId.toString,
              USER.FIRSTNAME.getName -> u.getFirstname,
              USER.LASTNAME.getName -> u.getLastname
            )
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

  def fetchUser(id: Long) =
    cache.get[UserRecord](id.toString).map { user =>
      Some(user)
    } getOrElse {
      db.query { sql =>
        val user = Option(
          sql.selectFrom[UserRecord](USER)
            .where(USER.ID.equal(id))
            .fetchOne()
        )
        user.foreach { u =>
          cache.set(u.getId.toString, u)
        }
        user
      }
    }


  def index = Authenticated { request =>
    Ok(views.html.index(request.firstName))

  }


/*
  def index = Action {

    Ok(views.html.index("Your new application is ready."))

  }*/

  def noob = Action {
    Ok("ha,noob man!")
  }


}

case class AuthenticatedRequest[A](userId:Long,firstName:String,lastName:String)

object Authenticated extends ActionBuilder[AuthenticatedRequest] with Results{



  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val authenticated = for{
      id <- request.session.get(USER.ID.getName)
      firstName <- request.session.get(USER.FIRSTNAME.getName)
      lastName <- request.session.get(USER.LASTNAME.getName)
    } yield {
      AuthenticatedRequest[A](id.toLong,firstName,lastName)
    }

    authenticated.map{ authenticatedRequest =>
      block(authenticatedRequest)
    } getOrElse {
      Future.successful {
        Redirect(routes.Application.login()).withNewSession
      }
    }
  }
}