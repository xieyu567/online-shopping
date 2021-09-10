package actors

import actors.BrowserManagerActor.AddBrowser
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.effe.shopping.shared.{Alarm, CartEvent}
import io.circe.syntax._
import io.circe.generic.auto._

import scala.collection.mutable.ListBuffer

object BrowserManagerActor {
    def props(): Props = Props(new BrowserManagerActor())

    case class AddBrowser(browser: ActorRef)
}

private class BrowserManagerActor() extends Actor with ActorLogging {
    val browsers: ListBuffer[ActorRef] = ListBuffer.empty[ActorRef]

    def receive: Receive = {
        case AddBrowser(b) =>
            context.watch(b)
            browsers += b
            log.info("websocket {} added", b.path)

        case CartEvent(user, product, action) =>
            val messageText = s"The user '$user' ${action.toString} ${product.name}"
            log.info("Sending alarm to all the brower with '{}' action: {}", messageText, action)
            browsers.foreach(_ ! Alarm(messageText, action).asJson.noSpaces)

        case Terminated(b) =>
            browsers -= b
            log.info("websocket {} removed", b.path)
    }
}
