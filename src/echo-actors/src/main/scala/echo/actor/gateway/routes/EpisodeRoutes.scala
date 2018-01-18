package echo.actor.gateway.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, _}
import org.slf4j.Logger
import echo.actor.gateway.json.JsonSupport
import echo.core.dto.{DTO, EpisodeDTO, PodcastDTO}

/**
  * @author Maximilian Irro
  */
class EpisodeRoutes(log: LoggingAdapter, getEpisode: String => EpisodeDTO) extends JsonSupport {

    val route = logRequestResult("EpisodeRoutes") {
        pathPrefix("episode"){
            pathEndOrSingleSlash {
                get {

                    // TODO get all Podcasts

                    complete(StatusCodes.NotImplemented)
                } ~
                    post {
                        entity(as[EpisodeDTO]) { podcastForCreate =>

                            // TODO create new podcast

                            complete(StatusCodes.NotImplemented)
                        }
                    }
            } ~
                pathPrefix(Segment) { echoId =>
                    get {
                        log.info("GET /api/episode/{}", echoId)
                        val episode = getEpisode(echoId)
                        if (episode != null) {
                            //println(episode)
                            complete(StatusCodes.OK, episode)
                        } else {
                            complete(StatusCodes.NotFound)
                        }

                    } ~
                        put {
                            entity(as[EpisodeDTO]) { podcastForUpdate =>

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
