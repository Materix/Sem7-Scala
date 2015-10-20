package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import scala.util.Random
import pl.suder.scala.auctionHouse.Message._

object Auction {
  val BidTimer = 10 seconds
  val DeleteTimer = 5 seconds
}

class Auction extends Actor {
  import context._
  import Auction._

  def Created: Receive = LoggingReceive {
    case Bid(bid) =>
      context become Activated(bid, sender)
    case EndAuction =>
      startDeleteTimer()
      context become Ignored
  }

  def Ignored: Receive = LoggingReceive {
    case Relist =>
      startEndTimer()
      context become Created
    case DeleteAuction =>
      context stop self
  }

  def Activated(prize: Int, buyer: ActorRef): Receive = LoggingReceive {
    case Bid(bid) if bid > prize =>
      buyer ! Beaten(bid)
      context become Activated(bid, sender)
    case Bid(_) => sender ! NotEnough(prize)
    case EndAuction =>
      buyer ! ItemBuyed
      // TODO notify Seller
      startDeleteTimer()
      context become Sold
  }

  def Sold: Receive = LoggingReceive {
    case Bid(_)        => sender ! ItemSold
    case DeleteAuction => context stop self
  }

  def startEndTimer() {
    system.scheduler.scheduleOnce(BidTimer, self, EndAuction)
  }

  def startDeleteTimer() {
    system.scheduler.scheduleOnce(DeleteTimer, self, DeleteAuction)
  }

  startEndTimer()
  def receive = Created
}