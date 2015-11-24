package pl.suder.scala.auctionHouse

import akka.actor._
import pl.suder.scala.auctionHouse.Message._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout

class NotifierRequest extends Actor {

  implicit val timeout = Timeout(5 seconds)

  override def receive = LoggingReceive {
    case x: Notify => {
      val actorRef = Await.result(context.actorSelection("akka.tcp://AuctionPublisher@127.0.0.1:2553/user/AuctionPublisher").resolveOne()(5 seconds), 5 seconds)
      val future = actorRef ? x
      val result = Await.result(future, timeout.duration)
      context stop self
    }
  }
}
