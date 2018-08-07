package echo.actor.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import echo.actor.ActorProtocol.{ActorRefBenchmarkMonitor, MessagePerSecondReport, StartMessagePerSecondMonitoring, StopMessagePerSecondMonitoring}
import echo.actor.index.IndexProtocol.{IndexResultsFound, NoIndexResultsFound, SearchIndex}
import echo.actor.index.IndexStoreSearchHandler.RefreshIndexSearcher
import echo.core.benchmark.{MessagesPerSecondCounter, RoundTripTime}
import echo.core.domain.dto.{IndexDocDTO, ResultWrapperDTO}
import echo.core.exception.SearchException
import echo.core.index.IndexSearcher

import scala.concurrent.blocking


/**
  * @author Maximilian Irro
  */

object IndexStoreSearchHandler {

    case object RefreshIndexSearcher

    def name(handlerIndex: Int): String = "handler-" + handlerIndex
    def props(indexSearcher: IndexSearcher): Props = {
        Props(new IndexStoreSearchHandler(indexSearcher))
            .withDispatcher("echo.index.dispatcher")
    }
}

class IndexStoreSearchHandler(indexSearcher: IndexSearcher) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private var currQuery: String = ""

    private var benchmarkMonitor: ActorRef = _

    private val mpsCounter = new MessagesPerSecondCounter()

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: SearchException =>
                log.error("Error trying to search the index; reason: {}", e.getMessage)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
                sender ! NoIndexResultsFound(currQuery, RoundTripTime.empty()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                currQuery = ""
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        Option(indexSearcher).foreach(_.destroy())
        log.info("shutting down")
    }


    override def receive: Receive = {

        case ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefBenchmarkMonitor(_)")
            benchmarkMonitor = ref

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsCounter.startCounting()

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            mpsCounter.stopCounting()
            benchmarkMonitor ! MessagePerSecondReport(self.path.toString, mpsCounter.getMessagesPerSecond)

        case RefreshIndexSearcher =>
            log.debug("Received RefreshIndexSearcher(_)")
            //val beforeRefresh = System.currentTimeMillis
            indexSearcher.refresh()
            //val afterRefresh = System.currentTimeMillis
            //log.info("[BENCH] Refresh took : {}ms", afterRefresh-beforeRefresh)

        case SearchIndex(query, page, size, rtt) =>
            log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)

            currQuery = query // make a copy in case of an exception
            //val beforeSearch = System.currentTimeMillis
            var results: ResultWrapperDTO = null
            blocking {
                results = indexSearcher.search(query, page, size)
            }

            //val afterSearch = System.currentTimeMillis
            //log.info("[BENCH] Search took : {}ms", afterSearch-beforeSearch)

            if(results.getTotalHits > 0){
                sender ! IndexResultsFound(query,results, rtt.bumpRTTs())
            } else {
                log.warning("No Podcast matching query: '{}' found in the index", query)
                sender ! NoIndexResultsFound(query, rtt.bumpRTTs())
            }

            mpsCounter.incrementCounter()
            currQuery = "" // wipe the copy

    }

}
