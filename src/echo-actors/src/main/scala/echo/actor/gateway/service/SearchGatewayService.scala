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
import echo.actor.ActorProtocol.{IndexResult, NoIndexResultsFound, SearchRequest, SearchResults}
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import io.swagger.annotations.{Api, ApiOperation}

import scala.concurrent.Await

/**
  * @author Maximilian Irro
  */
@Path("/api/search")  // @Path annotation required for Swagger
@Api(value = "/api/search",
    produces = "application/json")
class SearchGatewayService(log: LoggingAdapter,
                           internalTimeout: Timeout)(implicit val context: ActorContext) extends GatewayService with Directives with JsonSupport {

    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
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
        parameters('q, 'p.as[Int].?, 's.as[Int].?) { (query, page, size) =>

            /*
            val p: Int = page match {
                case Some(x) => x.toInt
                case None    => DEFAULT_PAGE
            }
            val s: Int = size match {
                case Some(x) => x.toInt
                case None    => DEFAULT_SIZE
            }
            */

            log.info("GET /api/search/?q={}&p={}&s={}", query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))

            onSuccess(searcher ? SearchRequest(query, page, size)) {
                case SearchResults(results) => complete(StatusCodes.OK, results)    // 200 all went well and we have results
                case NoIndexResultsFound(_) => complete(StatusCodes.NoContent)      // 204 we did not find anything
                case IndexRetrievalTimeout  => {
                    log.error("Timeout during search in SearchService")
                    complete(StatusCodes.RequestTimeout)                            // 408 search took too long
                }
                case _ => {
                    log.error("Received unhandled message on search request")
                    complete(StatusCodes.InternalServerError)                       // 500 generic server side error
                }
            }

        }
    }
}
