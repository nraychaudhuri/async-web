package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.Promise
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  
  val aPromise = Promise[Int]()

  def waiting = Action {
    AsyncResult(aPromise.future.map(i => Ok(i.toString)))
  }
  def redeem(i:Int) = Action {

    aPromise.success(i)
    Ok("thanks")

  }
  def index = Action {
    Ok(views.html.index())
  }
  
}
