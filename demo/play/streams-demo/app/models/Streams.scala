package models

trait Event

case class Operation(level: String, amout: Int) extends Event
case class SystemStatus(message: String) extends Event

object Stream {
  
  import scala.util.Random
  
  import play.api.libs.iteratee._
  import play.api.libs.concurrent.Akka
  import scala.concurrent.{Promise, Future}
  import scala.concurrent.duration._
  import play.api.Play.current

  import play.api.libs.concurrent.Execution.Implicits._
  
  val operations: Enumerator[Event] = Enumerator.generateM[Event] {
    val p = Promise[Option[Event]]()
    Akka.system.scheduler.scheduleOnce(50 milliseconds) {
      p.success(Some(Operation( if(Random.nextBoolean) "public" else "private", Random.nextInt(1000))))
    }
    p.future 
    //Promise.timeout(Some(Operation( if(Random.nextBoolean) "public" else "private", Random.nextInt(1000))), Random.nextInt(500))
  }
  
  val noise: Enumerator[Event] = Enumerator.generateM[Event] {
    val p = Promise[Option[Event]]()
    Akka.system.scheduler.scheduleOnce(50 milliseconds) {
      p.success(Some(SystemStatus("System message")))
    }
    p.future 

    //Promise.timeout(Some(SystemStatus("System message")), Random.nextInt(5000))
  }
  
  val events: Enumerator[Event] = operations.interleave(noise)
  
}
