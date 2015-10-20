package pl.suder.scala.auctionHouse

import akka.actor.{ ActorRef, FSM }
import scala.concurrent.duration.`package`.DurationInt
import pl.suder.scala.auctionHouse.Message._

object AuctionFSM {
  sealed trait State
  case object Created extends State
  case object Ignored extends State
  case object Activated extends State
  case object Sold extends State

  sealed trait Data
  case class Auction(winner: ActorRef, prize: Int) extends Data

  val BidTimer = 10 seconds
  val DeleteTimer = 5 seconds
}

class AuctionFSM extends FSM[AuctionFSM.State, AuctionFSM.Data] {
  import AuctionFSM._
  import context._

  startWith(Created, null)

  when(Created) {
    case Event(Bid(bid), _)   => goto(Activated) using (Auction(sender, bid))
    case Event(EndAuction, _) => goto(Ignored)
  }

  when(Ignored) {
    case Event(Relist, _) =>
      startEndTimer()
      goto(Created)
    case Event(DeleteAuction, _) => stop
  }

  when(Activated) {
    case Event(Bid(bid), Auction(buyer, prize)) if bid > prize =>
      buyer ! Beaten(bid)
      stay using (Auction(sender, bid))
    case Event(Bid(_), Auction(buyer, prize)) =>
      sender ! NotEnough(prize)
      stay
    case Event(EndAuction, Auction(buyer, prize)) =>
      buyer ! ItemBuyed
      // TODO notify Seller
      startDeleteTimer()
      goto(Sold)
  }

  when(Sold) {
    case Event(Bid(_), _) =>
      sender ! ItemSold
      stay
    case Event(DeleteAuction, _) => stop()
  }

  def startEndTimer() {
    system.scheduler.scheduleOnce(BidTimer, self, EndAuction)
  }

  def startDeleteTimer() {
    system.scheduler.scheduleOnce(DeleteTimer, self, DeleteAuction)
  }

  startEndTimer()
  initialize()
}