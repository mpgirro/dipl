package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, TooManyRequests}
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, ask}
import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.index.IndexProtocol.{IndexQuery, IndexResultsFound, NoIndexResultsFound, SearchIndex}
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.benchmark.rtt.{ImmutableRoundTripTime, RoundTripTime}
import echo.core.domain.dto.{ModifiableIndexDocDTO, ResultWrapperDTO}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object FutureSearcherWorker {
    def name(workerIndex: Int): String = "worker-" + workerIndex
    def props(): Props = Props(new FutureSearcherWorker()).withDispatcher("echo.searcher.dispatcher")
}

/**
  * This is an Implementation of the
  */
class FutureSearcherWorker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)
    private val INTERNAL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private val BREAKER_CALL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-call-timeout")).getOrElse(5).seconds
    private val BREAKER_RESET_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-reset-timeout")).getOrElse(10).seconds
    private val MAX_BREAKER_FAILURES: Int = 2 // TODO read from config

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.searcher.dispatcher")


    private val mediator = DistributedPubSub(context.system).mediator
    mediator ! Put(self) // register to the path

    private var indexStore: ActorRef = _
    private var benchmarkMonitor: ActorRef = _
    private var supervisor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter(self.path.toStringWithoutAddress)

    private val breaker =
        CircuitBreaker(context.system.scheduler, MAX_BREAKER_FAILURES, BREAKER_CALL_TIMEOUT, BREAKER_RESET_TIMEOUT)
            .onOpen(breakerOpen("Index"))
            .onClose(breakerClose("Index"))
            .onHalfOpen(breakerHalfOpen("Index"))

    /*
    val breaker =
        context.actorOf(
            CircuitBreakerPropsBuilder(maxFailures = MAX_BREAKER_FAILURES, callTimeout = BREAKER_CALL_TIMEOUT, resetTimeout = BREAKER_RESET_TIMEOUT)
                .props(target = indexStore),
            "indexCircuitBreaker")
    */

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref

        case ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefBenchmarkMonitor(_)")
            benchmarkMonitor = ref

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMeter.startMeasurement()

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            mpsMeter.stopMeasurement()
            benchmarkMonitor ! MessagePerSecondReport(mpsMeter.getResult)
            supervisor ! ChildMpsReport(mpsMeter.getResult)

        case SearchRequest(query, page, size, originalRtt) =>
            log.debug("Received SearchRequest('{}',{},{})", query, page, size)
            mpsMeter.tick()

            // TODO do some query processing (like extracting "sort:date:asc" and "sort:date:desc")

            val p: Int = page match {
                case Some(x) => x
                case None    => DEFAULT_PAGE
            }
            val s: Int = size match {
                case Some(x) => x
                case None    => DEFAULT_SIZE
            }

            if (isNullOrEmpty(query)) {
                sender ! SearchResults(ResultWrapperDTO.empty(), RoundTripTime.empty())
            }

            if (p < 0) {
                sender ! SearchResults(ResultWrapperDTO.empty(), RoundTripTime.empty())
            }

            if (s < 0) {
                sender ! SearchResults(ResultWrapperDTO.empty(), RoundTripTime.empty())
            }

            val originalSender = sender // this is important to not expose the handler

            val call = indexStore.ask(SearchIndex(query, p, s, originalRtt.bumpRTTs()))(INTERNAL_TIMEOUT)

            breaker.withCircuitBreaker(call).onComplete {
                case Success(res) =>
                    res match {
                        case IndexResultsFound(q2, resultWrapper: ResultWrapperDTO, returnRtt) =>
                            log.info("Received {} results from index for query '{}'", resultWrapper.getTotalHits, q2)

                            resultWrapper.getResults
                                .asScala
                                .map(r => new ModifiableIndexDocDTO().from(r))
                                .map(r => {
                                    Option(r.getDescription).map(value => {
                                        val soupDoc = Jsoup.parse(value)
                                        soupDoc.select("img").remove
                                        // if not removed, the cleaner will drop the <div> but leave the inner text
                                        val clean = Jsoup.clean(soupDoc.body.html, Whitelist.basic)
                                        r.setDescription(clean)
                                    })
                                    r
                                })
                                .map(r => r.toImmutable)

                            originalSender ! SearchResults(resultWrapper, returnRtt.bumpRTTs())

                        case NoIndexResultsFound(q2, returnRtt) =>
                            log.info("Received NO results from index for query '{}'", q2)
                            originalSender ! SearchResults(ResultWrapperDTO.empty(), returnRtt.bumpRTTs())

                        case IndexRetrievalTimeout  =>
                            log.error("Timeout during search in SearchService")
                            originalSender ! NoIndexResultsFound(query, originalRtt.bumpRTTs())
                        case unknown =>
                            log.error("Received unhandled message on search request of class : {}", unknown.getClass)
                        // 500 generic server side error
                    }

                //Circuit breaker opened handling
                case Failure(ex: CircuitBreakerOpenException) =>
                    log.error("CircuitBreakerOpenException calling Searcher -- returning {}: {}", TooManyRequests.intValue, TooManyRequests.defaultMessage)
                    originalSender ! NoIndexResultsFound(query, originalRtt.bumpRTTs())

                //General exception handling
                case Failure(ex) =>
                    log.error("Exception while calling Searcher with query : {}", query)
                    ex.printStackTrace()
                    originalSender ! NoIndexResultsFound(query, originalRtt.bumpRTTs())
            }
    }

    private def breakerOpen(name: String): Unit = {
        log.warning("{} Circuit Breaker is open", name)
    }

    private def breakerClose(name: String): Unit = {
        log.warning("{} Circuit Breaker is closed", name)
    }

    private def breakerHalfOpen(name: String): Unit = {
        log.warning("{} Circuit Breaker is half-open, next message goes through", name)
    }
}
