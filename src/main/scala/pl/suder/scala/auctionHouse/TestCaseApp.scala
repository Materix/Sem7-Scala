package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration.`package`.DurationInt
import com.typesafe.config._
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.Calendar

import pl.suder.scala.auctionHouse._
import pl.suder.scala.publisher._
import pl.suder.scala.auctionHouse.Message._

// Zaimplementować nastepujący scenariusz testowy:
// W pierwszym kroku tworzymy jednego aktora Seller, który rejestruje
// dużą ilość (np. 50000) aukcji,
// W drugim kroku (po zarejestrowaniu wszystkich aukcji) tworzymy jednego aktora
// Buyer, który wykonuje dużo (np. 10000) zapytań wyszukujących.
//
// Aby upewnić sie, że krok 1 zostanie zakonczony, zanim nastapi krok 2,
// nie należy używać sleep, można stworzyć pomocniczego aktora,
// który realizuje każdy krok testowy jako osobny stan.
//
// wykonać testy wydajnościowe mierzące, jak długo trwa obsłużenie wszystkich
// zapytań wyszukujących w zależności od różnej ilości aktorów AuctionSearch.
// Zwrócić uwagę na związek z ilością corów na maszynie, na ktorej wykonywane sa testy.
// porównać wyniki dla RoundRobinRoutingLogic oraz SmallestMailboxRoutingLogic

object TestCase {
  val searchWorkers = 16
  val auctions = 10000
  val searchs = 2000

  def currentTime: FiniteDuration = Duration(Calendar.getInstance.getTimeInMillis, MILLISECONDS)

  case object Ok
}

class TestCaseAuction(val name: String) extends Actor {
  override def receive = {
    case x =>
  }
  context.actorSelection("/user/MasterSearch") ! Register(name);
  context.parent ! Ack
}

class TestCaseBuyer extends Actor {
  var time: FiniteDuration = 0 millisecond;

  var receiveMsg = 0;

  override def receive = {
    case x: SearchResult if receiveMsg == TestCase.searchs => println(TestCase.currentTime - time); context.system.terminate();
    case x: SearchResult                                   => receiveMsg += 1
  }

  time = TestCase.currentTime
  0 to TestCase.searchs foreach { name => context.actorSelection("/user/MasterSearch") ! Search(name.toString()) }
}

class TestCaseSeller extends Actor {
  var receiveMsg = 0;

  override def receive = {
    case Ack if receiveMsg == TestCase.auctions => context.parent ! TestCase.Ok
    case Ack                                    => receiveMsg += 1
  }

  0 to TestCase.auctions foreach { name => context.actorOf(Props(classOf[TestCaseAuction], name.toString())) }
}

class TestCaseActor extends Actor {
  override def receive = {
    case TestCase.Ok => context.actorOf(Props[TestCaseBuyer], "buyer")
  }

  context.actorOf(Props[TestCaseSeller], "seller")
}

object TestCaseApp extends App {
  val config = ConfigFactory.load()
  val auctionPublisherSystem = ActorSystem("AuctionPublisher", config.getConfig("AuctionPublisher").withFallback(config))
  val auctionPublisher = auctionPublisherSystem.actorOf(Props[AuctionPublisher], "AuctionPublisher")

  val auctionHouseSystem = ActorSystem("AuctionHouse", config.getConfig("AuctionHouse").withFallback(config))
  auctionHouseSystem.actorOf(Props(classOf[MasterSearch], TestCase.searchWorkers), "MasterSearch")
  auctionHouseSystem.actorOf(Props[Notifier], "Notifier")

  auctionHouseSystem.actorOf(Props[TestCaseActor], "testCaseActor")

  Await.result(auctionHouseSystem.whenTerminated, Duration.Inf)
  auctionPublisherSystem.terminate()
}
