package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import echo.actor.gateway.json.{ArrayWrapper, JsonSupport}
import echo.actor.protocol.ActorMessages.{LoadTestFeeds, NoDocumentFound, PodcastResult}
import echo.core.model.dto.FeedDTO
import io.swagger.annotations._

/**
  * @author Maximilian Irro
  */
@Path("/api/feed")  // @Path annotation required for Swagger
@Api(value = "/api/feed",
    produces = "application/json")
class FeedGatewayService(log: LoggingAdapter,
                         internalTimeout: Timeout)(implicit val context: ActorContext) extends Directives with JsonSupport {

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    var directoryStore: ActorRef = _

    implicit val timeout = internalTimeout

    val route = pathPrefix("feed") { pathEndOrSingleSlash { getAllFeeds ~ postFeed } } ~
        pathPrefix("feed" / Segment) { id =>
            pathEndOrSingleSlash{ getFeed(id) ~ putFeed(id) ~ deleteFeed(id) }
        }

    def setDirectoryStoreActorRef(directoryStore: ActorRef) = this.directoryStore = directoryStore

    def getAllFeeds: Route = get {

        // TODO

        complete(StatusCodes.NotImplemented)
    }

    def getFeed(id: String): Route = get {

        // TODO

        complete(StatusCodes.NotImplemented)
    }

    def postFeed: Route = post {
        entity(as[FeedDTO]) { feed =>

            // TODO

            complete(StatusCodes.NotImplemented)
        }
    }

    def putFeed(id: String): Route = put {
        entity(as[FeedDTO]) { feed =>

            // TODO

            complete(StatusCodes.NotImplemented)
        }
    }

    def deleteFeed(id: String): Route = delete {

        // TODO

        complete(StatusCodes.NotImplemented)
    }

}
