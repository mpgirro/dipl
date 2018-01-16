package echo.actor.gateway.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import org.slf4j.Logger
import echo.actor.gateway.json.JsonSupport
import echo.core.dto.document.{DTO, EpisodeDTO, PodcastDTO}

/**
  * @author Maximilian Irro
  */
class PodcastRoutes(log: LoggingAdapter) extends JsonSupport {

    val route = logRequestResult("PodcastRoutes") {
        pathPrefix("podcast"){
            pathEndOrSingleSlash {
                get {

                    // TODO get all Podcasts

                    complete(StatusCodes.NotImplemented)
                } ~
                    post {
                        entity(as[PodcastDTO]) { podcastForCreate =>

                            // TODO create new podcast

                            complete(StatusCodes.NotImplemented)
                        }
                    }
            } ~
                pathPrefix(Segment) { echoId =>
                    get {

                        // TODO get podcast with echoId

                        complete(StatusCodes.NotImplemented)
                    } ~
                        put {
                            entity(as[PodcastDTO]) { podcastForUpdate =>

                                // TODO update podcast with echoId

                                complete(StatusCodes.NotImplemented)
                            }
                        } ~
                        delete {

                            // TODO delete podcast -  I guess this should not be supported?

                            complete(StatusCodes.NotImplemented)
                        }
                }

        }
    }
}
