package pl.suder.scala.auctionHouse

import akka.actor._
import akka.testkit.{ TestProbe, ImplicitSender, TestActorRef, TestKit }
import org.scalatest.{ Matchers, OneInstancePerTest, WordSpecLike }
import pl.suder.scala.auctionHouse._
import pl.suder.scala.auctionHouse.Message._
import scala.concurrent.duration.`package`.DurationInt

class AuctionSearchTest extends TestKit(ActorSystem("AuctionHouseTest"))
    with WordSpecLike with Matchers with ImplicitSender with OneInstancePerTest {
  val NAME = "name"
  val NAME_A = "ala ma kota"
  val NAME_B = "ala nie ma kota"
  val QUERY = "ala"

  val underTest = TestActorRef(Props(classOf[AuctionSearch]), "AuctionSearch")

  "An auction search " should {
    "send empty list" when {
      "there is no registered auction" in {
        underTest ! Search(NAME)
        expectMsg(1 second, SearchResult(List()))
      }
    }
    "send list with one element" when {
      "register one auction" in {
        val auction = TestProbe()
        auction.send(underTest, Register(NAME))
        underTest ! Search(NAME)
        expectMsg(1 second, SearchResult(List(auction.ref.path)))
      }
    }
    "send list without element" when {
      "register one auction and query is different" in {
        val auction = TestProbe()
        auction.send(underTest, Register(NAME_A))
        underTest ! Search(NAME)
        expectMsg(1 second, SearchResult(List()))
      }
    }
    "send list with two element" when {
      "register two auction and query is word which is in both name" in {
        val auction1 = TestProbe()
        auction1.send(underTest, Register(NAME_A))
        val auction2 = TestProbe()
        auction2.send(underTest, Register(NAME_B))
        underTest ! Search(QUERY)
        expectMsg(1 second, SearchResult(List(auction1.ref.path, auction2.ref.path)))
      }
    }
  }
}
