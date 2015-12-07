package pl.suder.scala.auctionHouse

import akka.actor._
import akka.event.LoggingReceive
import pl.suder.scala.auctionHouse.Message._
import akka.routing._

// MasterSearch zarządza wieloma aktorami AuctionSearch poprzez mechanizm routingu
// Aby każdy AuctionSearch miał wiedzę o wszystkich aukcjach, aktorzy Seller rejestrują aukcje poprzez MasterSearch poprzez logikę routera BroadcastRoutingLogic
// Aktorzy Buyer dokonują zapytań wyszukiwania również poprzez MasterSearch poprzez logike routera RoundRobinRoutingLogic

class MasterSearch(val workersNumber: Int) extends Actor {

  val workers = Vector.fill(workersNumber) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  val registerRouter = Router(BroadcastRoutingLogic(), workers)

  //  val searchRouter = Router(SmallestMailboxRoutingLogic(), workers)
  val searchRouter = Router(RoundRobinRoutingLogic(), workers)

  override def receive = {
    case x: Register   => registerRouter.route(x, sender)
    case x: Unregister => registerRouter.route(x, sender)
    case x: Search     => searchRouter.route(x, sender)
  }
}
