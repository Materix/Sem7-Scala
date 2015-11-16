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

import akka.testkit.{ TestProbe, ImplicitSender, TestActorRef, TestKit }
import org.scalatest.{ Matchers, OneInstancePerTest, WordSpecLike }

case object GET

class SaveTestActor extends PersistentActor {
  override def recovery = Recovery.none // disable recovering

  var time: FiniteDuration = 0 millisecond;

  def currentTime: FiniteDuration = Duration(Calendar.getInstance.getTimeInMillis, MILLISECONDS)

  override def persistenceId: String = "dbTest"

  override def receiveCommand: Receive = {
    case GET => {
      println("Write: " + time)
      val underTest2 = context.system.actorOf(Props[ReadTestActor], "readTestActor")
    }
    case x => {
      val start = currentTime
      persist(x) {
        _ => time += currentTime - start
      }
    }
  }
  override def receiveRecover: Receive = { // stub
    case _ =>
  }

  override def receive = receiveCommand
}

class ReadTestActor extends PersistentActor {
  override def recovery = Recovery(toSequenceNr = 1000L)

  var time: FiniteDuration = 0 millisecond;

  def currentTime: FiniteDuration = Duration(Calendar.getInstance.getTimeInMillis, MILLISECONDS)

  override def persistenceId: String = "dbTest"

  override def receiveCommand: Receive = {
    case GET => {
      println(time)
    }
  }
  override def receiveRecover: Receive = { // stub
    case RecoveryCompleted => println("Read: " + (currentTime - start)); context.system.terminate();
  }

  override def receive = receiveCommand
  val start = currentTime
}

object DBTest extends App {
  val system = ActorSystem("AuctionHouse")
  val underTest = system.actorOf(Props[SaveTestActor], "saveTestActor")

  1 to 1000 foreach { i => underTest ! Bid(i) }
  underTest ! GET

}
