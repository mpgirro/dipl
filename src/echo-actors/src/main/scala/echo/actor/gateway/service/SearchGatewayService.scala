package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.gateway.json.JsonSupport
import echo.actor.ActorProtocol.{SearchRequest, SearchResults}
import echo.actor.index.IndexProtocol.{IndexResult, NoIndexResultsFound}
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import io.swagger.annotations.{Api, ApiOperation}

import scala.concurrent.Await

/**
  * @author Maximilian Irro
  */
@Path("/api/search")  // @Path annotation required for Swagger
@Api(value = "/api/search",
    produces = "application/json")
class SearchGatewayService (private val log: LoggingAdapter)
                           (private implicit val context: ActorContext,
                            private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = CONFIG.getInt("echo.gateway.default-page")
    private val DEFAULT_SIZE: Int = CONFIG.getInt("echo.gateway.default-size")

    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var searcher: ActorRef = _

    override val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("search"){ pathEndOrSingleSlash { search } }

    def setSearcherActorRef(searcher: ActorRef): Unit = this.searcher = searcher

    @ApiOperation(value = "Search the index",
        nickname = "search",
        httpMethod = "GET",
        response = classOf[IndexResult],
        responseContainer = "Set")
    def search: Route = get {
        parameters('q, 'p.as[Int].?, 's.as[Int].?) { (query, page, size) =>
            log.info("GET /api/search/?q={}&p={}&s={}", query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))

            onSuccess(searcher ? SearchRequest(query, page, size)) {
                case SearchResults(results) => complete(StatusCodes.OK, results)    // 200 all went well and we have results
                case NoIndexResultsFound(_) => complete(StatusCodes.NoContent)      // 204 we did not find anything
                case IndexRetrievalTimeout  =>
                    log.error("Timeout during search in SearchService")
                    complete(StatusCodes.RequestTimeout) // 408 search took too long
                case _ =>
                    log.error("Received unhandled message on search request")
                    complete(StatusCodes.InternalServerError) // 500 generic server side error
            }

        }
    }
}
