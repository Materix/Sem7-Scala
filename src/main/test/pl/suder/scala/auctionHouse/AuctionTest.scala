package pl.suder.scala.auctionHouse

import akka.actor._
import akka.testkit.{ TestProbe, ImplicitSender, TestActorRef, TestKit }
import org.scalatest.{ Matchers, OneInstancePerTest, WordSpecLike }
import pl.suder.scala.auctionHouse._
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class AuctionTest extends TestKit(ActorSystem("AuctionHouseTest"))
    with WordSpecLike with Matchers with ImplicitSender with OneInstancePerTest {
  val NAME = "name"

  val parentTestProbe = TestProbe()
  val auctionSearchTestProbe = TestProbe()
  system.actorOf(Props(new Actor() {
    override def receive = {
      case x => auctionSearchTestProbe.ref forward x
    }
  }), "AuctionSearch")
  val underTest = TestActorRef(Props(classOf[Auction], NAME), parentTestProbe.ref, NAME)

  "An auction" should {
    "send ItemBuyed after AuctionEnded to winner" in {
      underTest ! Bid(1)
      expectMsg(Auction.BidTimer + (1 second), ItemBuyed)
    }
    "tell parent that item is sold" when {
      "sb bid it" in {
        underTest ! Bid(1)
        parentTestProbe.expectMsg(Auction.BidTimer + (1 second), AuctionEnded(true))
      }
    }
    "tell parent that auction is deleted" when {
      "sb does not bid it" in {
        parentTestProbe.expectMsg(Auction.BidTimer + (1 second) + Auction.DeleteTimer, AuctionDeleted)
      }
      "sb bid it" in {
        underTest ! Bid(1)
        parentTestProbe.expectMsg(Auction.BidTimer + (1 second), AuctionEnded(true))
        parentTestProbe.expectMsg(Auction.BidTimer + (1 second) + Auction.DeleteTimer, AuctionDeleted)
      }
    }
    "tell winner that is beaten" in {
      val buyer2 = TestProbe()
      underTest ! Bid(1)
      buyer2.send(underTest, Bid(2))
      expectMsg(1 second, Beaten(2))
    }
    "tell buyer that give too small bid" in {
      val buyer2 = TestProbe()
      underTest ! Bid(2)
      buyer2.send(underTest, Bid(1))
      buyer2.expectMsg(1 second, NotEnough(2))
    }
    "tell buyer that item is sold" when {
      "he send bid after auction is end" in {
        val buyer2 = TestProbe()
        underTest ! Bid(1)
        Thread sleep (Auction.BidTimer + (1 second)).length * 1000
        buyer2.send(underTest, Bid(2))
        buyer2.expectMsg(1 second, ItemSold)
      }
    }
    "be terminated after auction end" in {
      watch(underTest)
      expectTerminated(underTest, Auction.BidTimer + (1 second) + Auction.DeleteTimer)
    }
    "register in auction search" in {
      auctionSearchTestProbe.expectMsg(1 second, Register(NAME))
    }
  }

}
