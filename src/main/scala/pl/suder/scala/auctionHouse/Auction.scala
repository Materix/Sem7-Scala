package pl.suder.scala.auctionHouse

import akka.actor._
import akka.persistence._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import scala.concurrent.duration.`package`.DurationLong
import scala.util.Random
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration._
import java.util.Calendar

object Auction {
  sealed trait State

  case object Created extends State
  case object Ignored extends State
  //  case object Activated extends State
  case object Sold extends State
  case object Deleted extends State

  sealed trait Event

  case class BidEvent(bid: Int, winner: ActorPath) extends Event
  case class ChangeState(newState: State) extends Event
  case class EndTime(time: FiniteDuration) extends Event
  case class DeleteTime(time: FiniteDuration) extends Event

  val BidTimer = 20 seconds
  val DeleteTimer = 10 seconds
}

class Auction(val name: String) extends PersistentActor {
  import context._
  import Auction._

  def currentTime: FiniteDuration = Duration(Calendar.getInstance.getTimeInMillis, MILLISECONDS)

  def updateBid(bid: Int, winner: ActorPath): Unit = context become Activated(bid, winner)

  def updateState(newState: State) {
    newState match {
      case Auction.Created => context become Created
      case Auction.Ignored => context become Ignored
      case Auction.Sold    => context become Sold
      case Auction.Deleted => context.actorSelection("/user/AuctionSearch") ! Unregister(name); context stop self
    }
  }

  def updateEndTimer(time: FiniteDuration) = {
    if (time - currentTime > Duration(0, MILLISECONDS)) {
      system.scheduler.scheduleOnce(time - currentTime, self, EndAuction)
    }
  }

  def updateDeleteTimer(time: FiniteDuration) = {
    if (time - currentTime > Duration(0, MILLISECONDS)) {
      system.scheduler.scheduleOnce(time - currentTime, self, DeleteAuction)
    }
  }

  // Members declared in akka.persistence.Eventsourced
  override def receiveRecover: Receive = LoggingReceive {
    case BidEvent(bid, winner) => updateBid(bid, winner)
    case ChangeState(newState) => updateState(newState)
    case RecoveryCompleted     => // register, start timer if needed
    case EndTime(time)         => updateEndTimer(time)
    case DeleteTime(time)      => updateDeleteTimer(time)
  }

  // Members declared in akka.persistence.PersistenceIdentity
  override def persistenceId: String = "auction/" + name

  def Created: Receive = LoggingReceive {
    case Bid(bid) =>
      persist(BidEvent(bid, sender.path)) {
        evn => updateBid(bid, sender.path)
      }
    case EndAuction =>
      startDeleteTimer(DeleteTimer)
      persist(ChangeState(Auction.Ignored)) {
        evn => updateState(Auction.Ignored)
      }
    //      context become Ignored
  }

  def Ignored: Receive = LoggingReceive {
    case Relist =>
      startEndTimer(BidTimer)
      persist(ChangeState(Auction.Created)) {
        evn => updateState(Auction.Created)
      }
    case DeleteAuction =>
      context.parent ! AuctionDeleted
      persist(ChangeState(Auction.Deleted)) {
        evn => updateState(Auction.Deleted)
      }
  }

  def Activated(prize: Int, buyer: ActorPath): Receive = LoggingReceive {
    case Bid(bid) if bid > prize =>
      context.actorSelection(buyer) ! Beaten(bid)
      persist(BidEvent(bid, sender.path)) {
        evn => updateBid(bid, sender.path)
      }
    case Bid(_) if sender.path != buyer => sender ! NotEnough(prize) // self-beat protection
    case Bid(_)                         => println("Self-beat protection")
    case EndAuction =>
      context.actorSelection(buyer) ! ItemBuyed
      context.parent ! AuctionEnded(true)
      startDeleteTimer(DeleteTimer)
      persist(ChangeState(Auction.Sold)) {
        evn => updateState(Auction.Sold)
      }
  }

  def Sold: Receive = LoggingReceive {
    case Bid(_) => sender ! ItemSold
    case DeleteAuction =>
      context.parent ! AuctionDeleted
      persist(ChangeState(Auction.Deleted)) {
        evn => updateState(Auction.Deleted)
      }
    //      context stop self
  }

  def startEndTimer(time: FiniteDuration) {
    persist(EndTime(currentTime + time)) {
      evn => updateEndTimer(evn.time)
    }
  }

  def startDeleteTimer(time: FiniteDuration) {
    persist(DeleteTime(currentTime + time)) {
      evn => updateDeleteTimer(evn.time)
    }
  }

  override def receiveCommand = Created

  context.actorSelection("/user/AuctionSearch") ! Register(name);
  startEndTimer(BidTimer) // it cause extra unhandled message
}
