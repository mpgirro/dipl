package echo.actor.gateway.routes

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, _}
import echo.core.dto.document.EpisodeDocument
import org.slf4j.Logger
import echo.actor.gateway.json.JsonSupport
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}

/**
  * @author Maximilian Irro
  */
class EpisodeRoutes(log: LoggingAdapter, getEpisode: String => EpisodeDocument) extends JsonSupport {

    val route = logRequestResult("EpisodeRoutes") {
        pathPrefix("episode"){
            pathEndOrSingleSlash {
                get {
                    /*
                    complete {
                        // TODO get all Podcasts
                    }
                    */
                    complete(StatusCodes.NotImplemented)
                } ~
                    post {
                        entity(as[EpisodeDocument]) { podcastForCreate =>
                            /*
                            complete {
                                // TODO create podcast
                            }
                            */
                            complete(StatusCodes.NotImplemented)
                        }
                    }
            } ~
                pathPrefix(Segment) { echoId =>
                    get {
                        /*
                        complete {
                            // TODO get podcast with echoId
                        }
                        */

                        /*
                        logRequest("GET-EPISODE") {
                            // use in-scope marshaller to create completer function
                            completeWith(instanceOf[EpisodeDocument]) { completer =>
                                // custom
                                log.info("GET /api/episode/{}", echoId)
                                val episode = getEpisode(echoId)
                                if (episode != null) {
                                    complete(StatusCodes.OK, episode)
                                } else {
                                    complete(StatusCodes.NotFound)
                                }
                            }
                        }
                        */
                        log.info("GET /api/episode/{}", echoId)
                        val episode = getEpisode(echoId)
                        if (episode != null) {
                            println(episode)
                            complete(StatusCodes.OK, episode)
                        } else {
                            complete(StatusCodes.NotFound)
                        }
                    } ~
                        put {
                            entity(as[EpisodeDocument]) { podcastForUpdate =>
                                /*
                                complete {
                                    // TODO update podcast with echoId
                                }
                                */
                                complete(StatusCodes.NotImplemented)
                            }
                        } ~
                        delete {
                            // TODO delete podcast -  I guess this should not be supported?
                            /*
                            onSuccess(hotelService.deleteHotel(id)) { _ =>
                                complete(NoContent)
                            }
                            */
                            complete(StatusCodes.NotImplemented)
                        }
                }

        }
    }
}
