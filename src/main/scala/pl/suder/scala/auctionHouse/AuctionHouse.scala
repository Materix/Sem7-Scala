package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import com.typesafe.config._
import scala.concurrent.Await
import scala.concurrent.duration._

import pl.suder.scala.auctionHouse._
import pl.suder.scala.publisher._

object AuctionHouse extends App {
  val config = ConfigFactory.load()
  val auctionPublisherSystem = ActorSystem("AuctionPublisher", config.getConfig("AuctionPublisher").withFallback(config))
  val auctionPublisher = auctionPublisherSystem.actorOf(Props[AuctionPublisher], "AuctionPublisher")

  val auctionHouseSystem = ActorSystem("AuctionHouse", config.getConfig("AuctionHouse").withFallback(config))
  auctionHouseSystem.actorOf(Props[AuctionSearch], "AuctionSearch")
  auctionHouseSystem.actorOf(Props[Notifier], "Notifier")

  auctionHouseSystem.actorOf(Props(classOf[Seller], List("a a", "b", "c")), "seler")

  0 to 1 foreach { i => auctionHouseSystem.actorOf(Props(classOf[Buyer], List("a", "a", "b", "c"), 15), "buyer" + i) }

  Await.result(auctionPublisherSystem.whenTerminated, Duration.Inf)
  Await.result(auctionHouseSystem.whenTerminated, Duration.Inf)
}
