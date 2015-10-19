package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.util.Random
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class Buyer(val auctions: List[ActorRef]) extends Actor {
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
