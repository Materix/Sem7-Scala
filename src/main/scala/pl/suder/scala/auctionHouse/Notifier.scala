package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt
import akka.actor.SupervisorStrategy.{ Restart, Stop }
import java.util.concurrent.TimeoutException

class Notifier extends Actor {
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 minute) {
    case _: ActorNotFound    => Restart
    case _: TimeoutException => Restart
    case e: Exception        => Stop
  }

  override def receive = LoggingReceive {
    case notify: Notify => context.actorOf(Props[NotifierRequest]) ! notify
  }
}
