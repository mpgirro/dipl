package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Send}
import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.index.IndexProtocol.{IndexQuery, SearchIndex}
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto.ResultWrapperDTO

import scala.concurrent.duration._
import scala.language.postfixOps

object DelegationSearcherWorker {
    def name(workerIndex: Int): String = "worker-" + workerIndex
    def props(): Props = Props(new DelegationSearcherWorker()).withDispatcher("echo.searcher.dispatcher")
}

/**
  * This is an Implementation of the
  */
class DelegationSearcherWorker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)
    private val INTERNAL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private val mediator = DistributedPubSub(context.system).mediator
    mediator ! Put(self) // register to the path

    private var indexStore: ActorRef = _
    private var benchmarkMonitor: ActorRef = _
    private var supervisor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter(self.path.toStringWithoutAddress)

    private var responseHandlerCounter = 0

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

        case SearchRequest(query, page, size, rtt) =>
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

            val originalSender = Some(sender) // this is important to not expose the handler

            responseHandlerCounter += 1

            val rtt2 = rtt.bumpRTTs()
            val responseHandler = context.actorOf(IndexStoreReponseHandler.props(indexStore, originalSender, rtt2, INTERNAL_TIMEOUT), s"handler-${responseHandlerCounter}")

            val indexQuery = SearchIndex(query, p, s, rtt2)
            indexStore.tell(indexQuery, responseHandler)
            //sendIndexQuery(indexQuery, responseHandler) // TODO this is distributed message routing, which I benchmarked very slow

    }

    /**
      * Sends index query message to one Index Store within the Cluster, prefering a locally available Store for speed.
      * This will expect the index to response with a message. This will not be handled by this Searcher, but delegated
      * to the responseHandler
      * @param queryMsg
      * @param responseHandler
      * @return Nothing
      */
    private def sendIndexQuery(queryMsg: IndexQuery, responseHandler: ActorRef): Unit = {
        log.debug("Sending query message to one index store in the cluster : {}", queryMsg)
        mediator.tell(Send("/user/node/index", queryMsg, localAffinity = true), responseHandler)
    }
}
