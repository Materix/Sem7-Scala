package pl.suder.scala.publisher

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._

class AuctionPublisher extends Actor {
  override def receive = LoggingReceive {
    case Notify(title, winner, price) => println("Current winner of action: " + title + " is " + winner + " with price " + price)
  }
}
