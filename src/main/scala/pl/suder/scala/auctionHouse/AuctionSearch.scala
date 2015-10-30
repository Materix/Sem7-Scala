package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._

class AuctionSearch extends Actor {
  def working(auctions: Map[String, ActorRef]): Receive = LoggingReceive {
    case Register(name) => context become working(auctions + (name -> sender))
    case Search(search) => sender ! SearchResult(auctions.filterKeys { name => name.contains(search) }.values.toList)
  }
  
  override def receive = working(Map())
}