package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.util.Random
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class Buyer(val auctionsName: List[String], val maxPrize: Int) extends Actor {
  import context._

  def bid(auction: ActorPath, bid: Int) = system.scheduler.scheduleOnce(Random.nextInt(2000) + 100 milliseconds) {
    context.actorSelection(auction) ! Bid(bid)
  }

  def buying(sold: Int, participatedAuctions: List[ActorPath]): Receive = LoggingReceive {
    case SearchResult(auctions) =>
      auctions.filter { auction => !participatedAuctions.contains(auction) }.foreach { auction => bid(auction, 10) }
      context become buying(sold, (participatedAuctions ::: auctions).distinct)
    case ItemSold | ItemBuyed if sold + 1 == participatedAuctions.size => context stop self
    case ItemSold | ItemBuyed => context become buying(sold + 1, participatedAuctions)
    case NotEnough(currentBid) if currentBid + 1 > maxPrize => if (sold + 1 == participatedAuctions.size) context stop self else context become buying(sold + 1, participatedAuctions)
    case NotEnough(currentBid) => bid(sender.path, currentBid + 1)
    case Beaten(currentBid) if currentBid + 1 > maxPrize => if (sold + 1 == participatedAuctions.size) context stop self else context become buying(sold + 1, participatedAuctions)
    case Beaten(currentBid) => bid(sender.path, currentBid + 1)
  }

  override def receive = buying(0, List())
  Thread sleep 2000
  auctionsName.foreach { name => context.actorSelection("/user/MasterSearch") ! Search(name) }
}
