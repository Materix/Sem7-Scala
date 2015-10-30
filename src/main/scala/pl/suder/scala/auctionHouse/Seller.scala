package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._

class Seller(val auctionName: List[String]) extends Actor {
  def receive(auctionEnded: Int): Receive = LoggingReceive {
    case AuctionEnded(sold) =>
      sold match {
        case true  => println("Item sold: " + sender)
        case false => println("Item was not sell: " + sender)
      }
      if (auctionEnded + 1 == auctionName.size) context stop self else context become receive(auctionEnded + 1)
  }

  auctionName foreach { name => context.actorOf(Props(classOf[Auction], name), name) }
  override def receive = receive(0)
}
