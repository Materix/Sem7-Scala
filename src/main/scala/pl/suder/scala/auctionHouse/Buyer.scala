package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.util.Random
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class Buyer(val auctionsName: List[String], val maxPrize: Int) extends Actor {
  import context._

  def bid(auction: ActorRef, bid: Int) = system.scheduler.scheduleOnce(Random.nextInt(2000) + 100 milliseconds, auction, Bid(bid))

  def buying(sold: Int, found: Int): Receive = LoggingReceive {
    case SearchResult(auctions) =>
      auctions.foreach { auction => bid(auction, 10) }
      context become buying(sold, found + auctions.size)
    case ItemSold | ItemBuyed if sold + 1 == found =>
      println("end" + self); context stop self
    case ItemSold | ItemBuyed =>
      println("win or sold" + self); context become buying(sold + 1, found)
    case NotEnough(currentBid) if currentBid + 1 > maxPrize => if (sold + 1 == found) context stop self else context become buying(sold + 1, found)
    case NotEnough(currentBid)                              => bid(sender, currentBid + 1)
    case Beaten(currentBid) if currentBid + 1 > maxPrize    => if (sold + 1 == found) context stop self else context become buying(sold + 1, found)
    case Beaten(currentBid)                                 => bid(sender, currentBid + 1)
  }

  override def receive = buying(0, 0)
  auctionsName.foreach { name => context.actorSelection("/user/AuctionSearch") ! Search(name) }
}
