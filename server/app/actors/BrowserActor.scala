package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.effe.shopping.shared.CartEvent
import io.circe.generic.auto._
import io.circe.parser.decode

object BrowserActor {
    def props(browserManager: ActorRef): Props = Props(new BrowserActor(browserManager))
}

class BrowserActor(browserManager: ActorRef) extends Actor with ActorLogging {
    def receive: Receive = {
        case msg: String =>
            log.info("Received JSON message: {}", msg)
            decode[CartEvent](msg) match {
                case Right(cartEvent) =>
                    log.info("Got {} message", cartEvent)
                    browserManager forward cartEvent
                case Left(error) => log.info("Unhandled message: {}", error)
            }
    }
}