package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.JsonSupport
import echo.actor.protocol.ActorMessages.{IndexResult, SearchRequest, SearchResults}
import io.swagger.annotations.{Api, ApiOperation}

import scala.concurrent.Await

/**
  * @author Maximilian Irro
  */
@Path("/api/search")  // @Path annotation required for Swagger
@Api(value = "/api/search",
    produces = "application/json")
class SearchService (log: LoggingAdapter,
                     internalTimeout: Timeout)(implicit val context: ActorContext) extends Directives with JsonSupport {

    val DEFAULT_PAGE = ConfigFactory.load().getInt("echo.gateway.default-page")
    val DEFAULT_SIZE = ConfigFactory.load().getInt("echo.gateway.default-size")

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    var searcher: ActorRef = _

    implicit val timeout = internalTimeout

    val route = pathPrefix("search"){ pathEndOrSingleSlash { search } }

    def setSearcherActorRef(searcher: ActorRef) = this.searcher = searcher

    @ApiOperation(value = "Search the index",
        nickname = "search",
        httpMethod = "GET",
        response = classOf[IndexResult],
        responseContainer = "Set")
    def search: Route = get {
        parameters('q, 'p.?, 's.?) { (query, page, size) =>

            val p: Int = page match {
                case Some(x) => x.toInt
                case None    => DEFAULT_PAGE
            }
            val s: Int = size match {
                case Some(x) => x.toInt
                case None    => DEFAULT_SIZE
            }

            //log.info("Received HTTP request /search?q={}&p={}&s={}", query, p, s)

            onSuccess(searcher ? SearchRequest(query, p, s)) {
                case SearchResults(results) => complete(StatusCodes.OK, results)
                case _ => {
                    log.error("Timeout during search in SearchService")
                    complete(StatusCodes.InternalServerError)
                }
            }

        }
    }
}
