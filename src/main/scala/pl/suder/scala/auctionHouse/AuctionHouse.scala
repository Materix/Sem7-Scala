package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt

import pl.suder.scala.auctionHouse._

object AuctionHouse extends App {
  val system = ActorSystem("AuctionHouse")
  system.actorOf(Props[AuctionSearch], "AuctionSearch")
  system.actorOf(Props(classOf[Seller], List("a a", "b", "c")), "seler")
  Thread sleep 2000
  0 to 1 foreach { i => system.actorOf(Props(classOf[Buyer], List("a", "a", "b", "c"), 15), "buyer" + i) }
  system.awaitTermination()
}
