package pl.suder.scala

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt

object Auction {
  case class Bid(bid: Int) {
    require(bid > 0)
  }
  case object Relist
  case object EndAuction
  case object DeleteAuction
  
  val BidTimer = 5 second
  val DeleteTimer = 2 second
}

object Person {
  case object Sold
  case object Buyed
}

class Auction extends Actor {
  import Auction._ 
  import scala.concurrent.ExecutionContext.Implicits.global
  
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
      context become Activated(bid, sender)
    case EndAuction =>
      buyer ! Person.Buyed
      // TODO notify Seller
      startDeleteTimer()
      context become Sold
  }
  
  def Sold: Receive = LoggingReceive {
    case DeleteAuction =>
      context stop self
  }
  
  def startEndTimer() {
    println("Start end timer")
    context.system.scheduler.scheduleOnce(BidTimer, self, EndAuction)
  }
  
  def startDeleteTimer() {
    context.system.scheduler.scheduleOnce(DeleteTimer, self, DeleteAuction)
  }
  
  def receive = Created
  startEndTimer()
}

class Buyer(val auctions: List[ActorRef]) extends Actor {
  import Auction._
  
  def receive: Receive = LoggingReceive {
    case _ => ???
  }
  
  while(!auctions.isEmpty) {
    Thread sleep 1000
    auctions.foreach { auction => auction ! Bid(10)}
  }
}

object AuctionHouse extends App {
  val system = ActorSystem("AuctionHouse")
  val auctions = List();
  val action = system.actorOf(Props[Auction], "auction")
  val buyer = system.actorOf(Props(classOf[Buyer], List(action)), "buyer")

  system.awaitTermination()
}