package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Promise

object Application extends Controller {

  //curl -X POST -d @Skype_5.8.0.1027.dmg http://127.0.0.1:9000
  
  //val bodyParser = BodyParser( request => Iteratee.fold[Array[Byte],Int](0)((c,_) => c+1 ).map(Right(_)))

  val slowBodyParser = BodyParser( request => Iteratee.foldM[Array[Byte],Int](0)((c,_) => Promise.timeout({ println("got a chunk!"); c+1} ,500) ).map(Right(_)))

  def index = Action(slowBodyParser) { rq =>
    Ok("got " + rq.body + " chunks")
  }
  
}
