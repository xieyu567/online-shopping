package controllers

import actors.{BrowserActor, BrowserManagerActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

import javax.inject.{Inject, Singleton}

@Singleton
class WebSockets @Inject()(implicit actorSystem: ActorSystem,
                           materializer: Materializer,
                           cc: ControllerComponents) extends AbstractController(cc) {
    val logger: Logger = Logger(this.getClass)
    val managerActor: ActorRef = actorSystem.actorOf(BrowserManagerActor.props(), "manager-actor")

    def cartEventWS: WebSocket = WebSocket.accept[String, String] {
        implicit request =>
            ActorFlow.actorRef { out =>
                logger.info(s"Got a new websocket connection from ${request.host}")
                managerActor ! BrowserManagerActor.AddBrowser(out)
                BrowserActor.props(managerActor)
            }
    }
}

