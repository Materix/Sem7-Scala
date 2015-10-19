package pl.suder.scala

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import scala.util.Random
import pl.suder.scala._

object AuctionMessage {
  case class Bid(bid: Int) {
    require(bid > 0)
  }
  case object Relist
  case object EndAuction
  case object DeleteAuction
  
  val BidTimer = 10 seconds
  val DeleteTimer = 5 seconds
  
  case object ItemSold
  case object ItemBuyed
  case object NotEnough
  case object Beaten
}

class Auction extends Actor {
  import AuctionMessage._ 
  import context._
  
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
      buyer ! Beaten
      context become Activated(bid, sender)
    case Bid(_) => sender ! NotEnough
    case EndAuction =>
      buyer ! ItemBuyed
      // TODO notify Seller
      startDeleteTimer()
      context become Sold
  }
  
  def Sold: Receive = LoggingReceive {
    case Bid(_) => sender ! ItemSold
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

class Buyer(val auctions: List[ActorRef]) extends Actor {
  import AuctionMessage._
  import context._
  
  def bid(auction: ActorRef, bid: Int) = system.scheduler.scheduleOnce(Random.nextInt(2000) + 100 milliseconds, auction, Bid(bid))

  def buying(sold: Int): Receive = LoggingReceive {
    case ItemSold | ItemBuyed if sold + 1 == auctions.size => context stop self 
    case ItemSold | ItemBuyed => context become buying(sold + 1)
    case NotEnough => bid(sender, 10)
    case Beaten => bid(sender, 10)
  }
  
  def receive = buying(0)
  auctions.foreach { auction => bid(auction, 10) }
}

object AuctionHouse extends App {
  val system = ActorSystem("AuctionHouse")
  
  var auctions: List[ActorRef] = List()
  0 to 10 foreach {i => auctions = (system.actorOf(Props[Auction], "auction" + i) :: auctions)}
//  0 to 10 foreach {i => auctions = (system.actorOf(Props[AuctionFSM], "auction" + i) :: auctions)}
  
  0 to 5 foreach {i => system.actorOf(Props(classOf[Buyer], auctions), "buyer" + i)}
  system.awaitTermination()
}