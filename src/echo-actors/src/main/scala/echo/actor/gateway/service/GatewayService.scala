package echo.actor.gateway.service

import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.server.Route

/**
  * @author Maximilian Irro
  */
trait GatewayService {

    val DISPATCHER_ID = "echo.gateway.dispatcher"
    implicit val blockingDispatcher: MessageDispatcher
    val route: Route

}
