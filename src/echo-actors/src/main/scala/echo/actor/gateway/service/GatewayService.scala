package echo.actor.gateway.service

import akka.http.scaladsl.server.{Directives, Route}
import echo.actor.gateway.json.JsonSupport

/**
  * @author Maximilian Irro
  */
trait GatewayService {

    val route: Route

}
