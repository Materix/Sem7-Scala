package pl.suder.scala.auctionHouse

import akka.actor._
import pl.suder.scala.auctionHouse.Message._
import akka.event.LoggingReceive

class NotifierRequest(val notifyMsg: Notify) extends Actor {
  override def receive = LoggingReceive {
    case x => println("Coś dziwnego: " + x);
  }
  context.actorSelection("akka.tcp://AuctionPublisher@127.0.0.1:2553/user/AuctionPublisher") ! notifyMsg
}
