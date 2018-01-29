package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import echo.actor.protocol.ActorMessages.{IndexResultsFound, NoIndexResultsFound, SearchResults}
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import echo.core.model.dto.ResultWrapperDTO
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object IndexStoreReponseHandler {

    case object IndexRetrievalTimeout

    def props(indexStore: ActorRef, originalSender: Option[ActorRef]): Props = {
        Props(new IndexStoreReponseHandler(indexStore, originalSender.get))
    }
}

class IndexStoreReponseHandler(indexStore: ActorRef, originalSender: ActorRef) extends Actor with ActorLogging {
    def receive = LoggingReceive {
        case IndexResultsFound(query: String, results: ResultWrapperDTO) => {
            log.info("Received " + results.getTotalHits + " results from index for query '" + query + "'")
            timeoutMessager.cancel

            // TODO remove <img> tags from description, I suspect it to cause troubles
            // TODO this should probably better done while indexing! (so only has to be done one time instead of every retrieval
            results.getResults.foreach( d => {
                Option(d.getDescription).map(value => {
                    val soupDoc = Jsoup.parse(value)
                    soupDoc.select("img").remove
                    // if not removed, the cleaner will drop the <div> but leave the inner text
                    val clean = Jsoup.clean(soupDoc.body.html, Whitelist.basic)
                    d.setDescription(clean)
                })
            })

            sendResponseAndShutdown(SearchResults(results))
        }
        case NoIndexResultsFound(query: String) => {
            log.info("Received NO results from index for query '" + query + "'")
            sendResponseAndShutdown(SearchResults(new ResultWrapperDTO))
        }
        case IndexRetrievalTimeout => sendResponseAndShutdown(IndexRetrievalTimeout)
        case _ =>
    }

    def sendResponseAndShutdown(response: Any) = {
        originalSender ! response
        log.debug("Stopping context capturing actor")
        context.stop(self)
    }

    import context.dispatcher
    val timeoutMessager = context.system.scheduler.
        scheduleOnce(5 seconds) {
            self ! IndexRetrievalTimeout
        }
}
