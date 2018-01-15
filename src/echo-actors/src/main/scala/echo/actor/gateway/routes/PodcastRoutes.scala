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
                    /*
                    complete {
                        // TODO get all Podcasts
                    }
                    */
                    complete(StatusCodes.NotImplemented)
                } ~
                    post {
                        entity(as[PodcastDTO]) { podcastForCreate =>
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
                     // debugging helper
                     logRequest("GET-PODCAST") {
                         // use in-scope marshaller to create completer function
                         completeWith(instanceOf[PodcastDocument]) { completer =>
                             // custom
                             val podcast = getPodcast(podcastId)
                             if(podcast != null){
                                 complete(StatusCodes.OK, podcast)
                             } else {
                                 complete(StatusCodes.NotFound)
                             }
                         }
                     }
                     */

                        complete(StatusCodes.NotImplemented)
                    } ~
                        put {
                            entity(as[PodcastDTO]) { podcastForUpdate =>
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
