package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt

import pl.suder.scala.auctionHouse._

object AuctionHouse extends App {
  val system = ActorSystem("AuctionHouse")

  var auctions: List[ActorRef] = List()
  //0 to 9 foreach { i => auctions = (system.actorOf(Props[Auction], "auction" + i) :: auctions) }
  //0 to 9 foreach { i => auctions = (system.actorOf(Props[AuctionFSM], "auction" + i) :: auctions) }

  //0 to 4 foreach { i => system.actorOf(Props(classOf[Buyer], auctions), "buyer" + i) }
  system.actorOf(Props[AuctionSearch], "AuctionSearch")
  system.actorOf(Props(classOf[Seller], List("a", "b", "c")), "seler")
  Thread sleep 100
  0 to 1 foreach { i => system.actorOf(Props(classOf[Buyer], List("a", "b", "c"), 15), "buyer" + i) }
  system.awaitTermination()
}
