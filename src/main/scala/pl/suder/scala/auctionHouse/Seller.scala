package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class Seller(val auctionName: List[String]) extends Actor {
  import context._
  def receive(auctionEnded: Int): Receive = LoggingReceive {
    case AuctionEnded(sold) =>
      sold match {
        case true  => println("Item sold: " + sender)
        case false => println("Item was not sell: " + sender)
      }
    case AuctionDeleted =>
      if (auctionEnded + 1 == auctionName.size) context.system.scheduler.scheduleOnce(1 second) { context stop self } else context become receive(auctionEnded + 1)
  }

  auctionName foreach { name => context.actorOf(Props(classOf[Auction], name), name.replace(" ", "_")) }
  override def receive = receive(0)
  context.parent ! Ack
}
