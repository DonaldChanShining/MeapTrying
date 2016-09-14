package helpers

import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext

/**
  *
  * Created by donglai.chen on 2016/8/12.
  *
  */
object Contexts {

  implicit val app = play.api.Play.current

  val database: ExecutionContext = Akka.system.dispatchers.lookup("contexts.database")

 // val myExecutionContext: ExecutionContext = akka.System.dispatchers.lookup("my-context")



}
