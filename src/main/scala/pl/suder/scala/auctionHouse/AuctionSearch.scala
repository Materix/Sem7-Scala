package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._

class AuctionSearch extends Actor {
  def working(auctions: Map[String, ActorPath]): Receive = LoggingReceive {
    case Register(name)   => context become working(auctions + (name -> sender.path))
    case Search(search)   => sender ! SearchResult(auctions.filterKeys { name => name.contains(" " + search + " ") || name.startsWith(search + " ") || name.endsWith(" " + search) || name.equals(search) }.values.toList)
    case Unregister(name) => context become working(auctions - name)
  }

  override def receive = working(Map())
}
