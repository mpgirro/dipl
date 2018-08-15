package echo.actor.gateway.service

import javax.ws.rs.Path

import akka.actor.{ActorContext, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, TooManyRequests}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{RetrievalSubSystemRoundTripTimeReport, SearchRequest, SearchResults}
import echo.actor.gateway.json.JsonSupport
import echo.actor.index.IndexProtocol.NoIndexResultsFound
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto.ResultWrapperDTO
import io.swagger.annotations._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */
@Path("/api/search")  // @Path annotation required for Swagger
@Api(value = "/api/search",
    produces = "application/json")
class SearchGatewayService (private val log: LoggingAdapter, private val breaker: CircuitBreaker, private val mpsMeter: MessagesPerSecondMeter)
                           (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = CONFIG.getInt("echo.gateway.default-page")
    private val DEFAULT_SIZE: Int = CONFIG.getInt("echo.gateway.default-size")

    private val SEARCHER_PATH = "/user/node/searcher"

    // TODO: reactivated local ref due to discovered bottleneck (benchmark - july 2018)
    // will be set after construction of the service via the setter method,
    // once the message with the reference arrived
    private var searcher: ActorRef = _
    private var benchmarkMonitor: ActorRef = _

    private val mediator = DistributedPubSub(context.system).mediator

    override implicit val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("search") { pathEndOrSingleSlash { search } }

    def setSearcherActorRef(searcher: ActorRef): Unit = this.searcher = searcher

    def setBenchmarkMonitorActorRef(benchmarkMonitor: ActorRef): Unit = this.benchmarkMonitor = benchmarkMonitor

    private def search: Route = get {
        parameters('q, 'p.as[Int].?, 's.as[Int].?) { (query, page, size) =>
            log.info("GET /api/search/?q={}&p={}&s={}", query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))
            mpsMeter.registerMessage()
            onCompleteWithBreaker(breaker)(searcher ? SearchRequest(query, page, size, RoundTripTime.empty())) {
                case Success(res) =>
                    res match {
                        case SearchResults(results,_) => complete(StatusCodes.OK, results)    // 200 all went well and we have results
                        case NoIndexResultsFound(_,_) => complete(StatusCodes.NoContent)      // 204 we did not find anything
                        case IndexRetrievalTimeout  =>
                            log.error("Timeout during search in SearchService")
                            complete(StatusCodes.RequestTimeout)                            // 408 search took too long
                        case _ =>
                            log.error("Received unhandled message on search request")
                            complete(StatusCodes.InternalServerError)                       // 500 generic server side error
                    }

                //Circuit breaker opened handling
                case Failure(ex: CircuitBreakerOpenException) =>
                    log.error("CircuitBreakerOpenException calling Searcher -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                    complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

                //General exception handling
                case Failure(ex) =>
                    log.error("Exception while calling Searcher with query : {}", query)
                    ex.printStackTrace()
                    complete(InternalServerError)
            }

        }
    }

    /*
    @Path("/search")
    @ApiOperation(
        value = "Return the search results found for the given query",
        nickname = "search",
        httpMethod = "GET",
        responseContainer = "Set")
    @ApiImplicitParams(Array(
        new ApiImplicitParam(
            name = "q",
            value = "The query for which will be searched",
            example = "freak show",
            defaultValue = "",
            required = true,
            dataType = "string",
            paramType = "query"),
        new ApiImplicitParam(
            name = "p",
            value = "The page of the search results, that should be returned",
            example = "1",
            defaultValue = "1",
            required = false,
            dataType = "number",
            paramType = "query"),
        new ApiImplicitParam(
            name = "s",
            value = "The size (= number of items) of the search result page",
            example = "20",
            defaultValue = "20",
            required = false,
            dataType = "number",
            paramType = "query")
    ))
    @ApiResponses(Array(
        new ApiResponse(code = 200, message = "Return search results", response = classOf[IndexResult]),
        new ApiResponse(code = 500, message = "Internal server error")
    ))
    */
    def distributedSearch: Route = get {
        parameters('q, 'p.as[Int].?, 's.as[Int].?) { (query, page, size) =>
            log.info("GET /api/search/?q={}&p={}&s={}", query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))
            mpsMeter.registerMessage()
            onCompleteWithBreaker(breaker)(emitSearchQuery(SearchRequest(query, page, size, RoundTripTime.empty()))) {
                case Success(res) =>
                    res match {
                        case SearchResults(results,_) => complete(StatusCodes.OK, results)    // 200 all went well and we have results
                        case NoIndexResultsFound(_,_) => complete(StatusCodes.NoContent)      // 204 we did not find anything
                        case IndexRetrievalTimeout  =>
                            log.error("Timeout during search in SearchService")
                            complete(StatusCodes.RequestTimeout)                            // 408 search took too long
                        case _ =>
                            log.error("Received unhandled message on search request")
                            complete(StatusCodes.InternalServerError)                       // 500 generic server side error
                    }

                //Circuit breaker opened handling
                case Failure(ex: CircuitBreakerOpenException) =>
                    log.error("CircuitBreakerOpenException calling Searcher -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                    complete(HttpResponse(TooManyRequests).withEntity("Server Busy"))

                //General exception handling
                case Failure(ex) =>
                    log.error("Exception while calling Searcher with query : {}", query)
                    ex.printStackTrace()
                    complete(InternalServerError)
            }

        }
    }



    /**
      * Sends message to a searcher within the cluster, NOT prefering locally available searchers,
      * because we more likely will operate only one gateway, but have multiple index stores
      * @param requestMsg
      * @return Future producing the result message
      */
    private def emitSearchQuery(requestMsg: SearchRequest): Future[Any] = {
        log.debug("Sending request message to some searcher in the cluster : {}", requestMsg)
        mediator ? Send(path = SEARCHER_PATH, msg = requestMsg, localAffinity = false)
    }

}
