package pl.suder.scala.auctionHouse

import akka.actor._
import akka.testkit.{ TestProbe, ImplicitSender, TestActorRef, TestKit }
import org.scalatest.{ Matchers, OneInstancePerTest, WordSpecLike }
import pl.suder.scala.auctionHouse._
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt
import org.scalatest.words.HaveWord

class SellerTest extends TestKit(ActorSystem("AuctionHouseTest"))
    with WordSpecLike with Matchers with ImplicitSender with OneInstancePerTest {
  val AUCTION_NAMES = List("a", "b", "c", "d")

  val underTest = TestActorRef(Props(classOf[Seller], AUCTION_NAMES), "seller")

  "A seller" should {
    "have " + AUCTION_NAMES.length + " children" in {
      underTest.children.size equals AUCTION_NAMES.length
    }
    "end when all children ends" in {
      watch(underTest)
      expectTerminated(underTest, Auction.BidTimer + Auction.DeleteTimer + (1 second))
    }
  }
}
