package pl.suder.scala.auctionHouse

import scala.concurrent.duration.`package`.DurationInt

object Message {
  case class Bid(bid: Int) {
    require(bid > 0)
  }
  case object Relist
  case object EndAuction
  case object DeleteAuction

  case object ItemSold
  case object ItemBuyed
  case class NotEnough(bid: Int) {
    require(bid > 0)
  }
  case class Beaten(bid: Int) {
    require(bid > 0)
  }
}