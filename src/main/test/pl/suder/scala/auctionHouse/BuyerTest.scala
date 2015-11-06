package pl.suder.scala.auctionHouse

import akka.actor._
import akka.testkit.{ TestProbe, ImplicitSender, TestActorRef, TestKit }
import org.scalatest.{ Matchers, OneInstancePerTest, WordSpecLike }
import pl.suder.scala.auctionHouse._
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class BuyerTest extends TestKit(ActorSystem("AuctionHouseTest"))
    with WordSpecLike with Matchers with ImplicitSender with OneInstancePerTest {
  val AUCTION_NAME = "name"

  val auctionSearchTestProbe = TestProbe()
  val auctionTestProbe = TestProbe()
  system.actorOf(Props(new Actor() {
    override def receive = {
      case x => sender ! SearchResult(List(auctionTestProbe.ref)); auctionSearchTestProbe.ref forward x
    }
  }), "AuctionSearch")
  val underTest = TestActorRef(Props(classOf[Buyer], List(AUCTION_NAME), 15), "buyer")

  "A buyer" should {
    "search an auction using given names" in {
      auctionSearchTestProbe.expectMsg(1 second, Search(AUCTION_NAME))
    }
    "try to bid an auction" in {
      auctionTestProbe.expectMsg(3 second, Bid(10))
    }
    "try to rebid an auction when was beaten" in {
      auctionTestProbe.expectMsg(3 second, Bid(10))
      auctionTestProbe.send(underTest, Beaten(11))
      auctionTestProbe.expectMsg(3 second, Bid(12))
    }
    "terminated" when {
      "buy item" in {
        watch(underTest)
        auctionTestProbe.expectMsg(3 second, Bid(10))
        auctionTestProbe.send(underTest, ItemBuyed)
        expectTerminated(underTest, 1 second)
      }
      "auction was ended (and he does not buy item)" in {
        watch(underTest)
        auctionTestProbe.expectMsg(3 second, Bid(10))
        auctionTestProbe.send(underTest, ItemSold)
        expectTerminated(underTest, 1 second)
      }
      "does not have enough money" in {
        watch(underTest)
        auctionTestProbe.expectMsg(3 second, Bid(10))
        auctionTestProbe.send(underTest, Beaten(20))
        expectTerminated(underTest, 1 second)
      }
    }
  }
}
