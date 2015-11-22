package pl.suder.scala.auctionHouse

import akka.actor.ActorPath
import scala.concurrent.duration.`package`.DurationInt

object Message {
  case class Bid(bid: Int) {
    require(bid > 0)
  }
  case object Relist
  case object EndAuction
  case object DeleteAuction
  case object AuctionDeleted
  case class AuctionEnded(sold: Boolean)

  case object ItemSold
  case object ItemBuyed
  case class NotEnough(bid: Int) {
    require(bid > 0)
  }
  case class Beaten(bid: Int) {
    require(bid > 0)
  }

  case class Register(name: String) {
    require(name != null)
    require(name.trim.length > 0)
  }

  case class Unregister(name: String) {
    require(name != null)
    require(name.trim.length > 0)
  }

  case class Search(name: String) {
    require(name != null)
    require(name.trim.length > 0)
  }

  case class SearchResult(auctions: List[ActorPath])

  case class Notify(title: String, winner: ActorPath, price: Int)
}
