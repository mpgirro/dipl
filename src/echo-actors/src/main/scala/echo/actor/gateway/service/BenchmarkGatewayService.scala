package echo.actor.gateway.service

import akka.actor.{ActorContext, ActorRef}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{RetrievalSubSystemRoundTripTimeReport, SearchRequest, SearchResults}
import echo.actor.gateway.json.JsonSupport
import echo.actor.index.IndexProtocol.NoIndexResultsFound
import echo.core.benchmark._
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.benchmark.rtt.{ImmutableRoundTripTime, RoundTripTime}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */
class BenchmarkGatewayService (private val log: LoggingAdapter, private val breaker: CircuitBreaker, private val mpsMeter: MessagesPerSecondMeter, private val gateway: ActorRef)
                            (private implicit val context: ActorContext, private implicit val timeout: Timeout) extends GatewayService with Directives with JsonSupport {


    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = CONFIG.getInt("echo.gateway.default-page")
    private val DEFAULT_SIZE: Int = CONFIG.getInt("echo.gateway.default-size")

    private val SEARCHER_PATH = "/user/node/searcher"

    private var searcher: ActorRef = _
    private var benchmarkMonitor: ActorRef = _

    private val mediator = DistributedPubSub(context.system).mediator

    override implicit val blockingDispatcher: MessageDispatcher = context.system.dispatchers.lookup(DISPATCHER_ID)

    override val route: Route = pathPrefix("benchmark-search") { pathEndOrSingleSlash { benchmarkSearchRoute } }


    def setSearcherActorRef(searcher: ActorRef): Unit = this.searcher = searcher

    def setBenchmarkMonitorActorRef(benchmarkMonitor: ActorRef): Unit = this.benchmarkMonitor = benchmarkMonitor

    def benchmarkSearch(query: String, page: Option[Int], size: Option[Int], rtt1: RoundTripTime): Unit = {
        // in benchmark mode, we do not use futures, but instead simply fire and forget
        searcher.tell(SearchRequest(query, page, size, rtt1.bumpRTTs()), gateway)
    }

    private def benchmarkSearchRoute: Route = get {
        parameters('q, 'p.as[Int].?, 's.as[Int].?) { (query, page, size) =>
            log.info("GET /benchmark/search/?q={}&p={}&s={}", query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))
            mpsMeter.tick()

            val rtt = ImmutableRoundTripTime.builder()
                .setId(query)
                .setLocation("")
                .setWorkflow(Workflow.RESULT_RETRIEVAL)
                .create()

            searcher.tell(SearchRequest(query, page, size, rtt.bumpRTTs()), gateway)

            complete(StatusCodes.OK)
        }
    }

    def benchmarkDistributedSearch(query: String, page: Option[Int], size: Option[Int], rtt1: RoundTripTime): Unit = {
        implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.gateway.dispatcher")
        emitSearchQuery(SearchRequest(query, page, size, rtt1.bumpRTTs()))
            .onComplete {
                case Success(res) =>
                    res match {
                        case SearchResults(results, rtt2) => benchmarkMonitor ! RetrievalSubSystemRoundTripTimeReport(rtt2.bumpRTTs())
                        case NoIndexResultsFound(q, rtt2) => benchmarkMonitor ! RetrievalSubSystemRoundTripTimeReport(rtt2.bumpRTTs())
                        case _ =>
                            log.error("[BENCH] Received unhandled message on search request")
                            benchmarkMonitor ! RetrievalSubSystemRoundTripTimeReport(rtt1.bumpRTTs())
                    }
                //Circuit breaker opened handling
                case Failure(ex: CircuitBreakerOpenException) =>
                    log.error("[BENCH] CircuitBreakerOpenException calling Searcher")
                    benchmarkMonitor ! RetrievalSubSystemRoundTripTimeReport(rtt1.bumpRTTs())

                //General exception handling
                case Failure(ex) =>
                    log.error("[BENCH] Exception while calling Searcher with query : {}", query)
                    ex.printStackTrace()
                    benchmarkMonitor ! RetrievalSubSystemRoundTripTimeReport(rtt1.bumpRTTs())
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
