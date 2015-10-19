package pl.suder.scala

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt

import pl.suder.scala.auctionHouse._

object AuctionHouse extends App {
  val system = ActorSystem("AuctionHouse")
  
  var auctions: List[ActorRef] = List()
//  0 to 9 foreach {i => auctions = (system.actorOf(Props[Auction], "auction" + i) :: auctions)}
  0 to 9 foreach {i => auctions = (system.actorOf(Props[AuctionFSM], "auction" + i) :: auctions)}
  
  0 to 4 foreach {i => system.actorOf(Props(classOf[Buyer], auctions), "buyer" + i)}
  system.awaitTermination()
}