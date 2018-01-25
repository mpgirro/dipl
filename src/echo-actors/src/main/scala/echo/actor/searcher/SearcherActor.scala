package echo.actor.searcher

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.{Logging, LoggingReceive}
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.protocol.ActorMessages.{IndexResultsFound, _}
import echo.actor.searcher.SearcherActor.IndexRetrievalTimeout
import echo.core.dto.{DTO, ResultWrapperDTO}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object SearcherActor {
    case object IndexRetrievalTimeout
}

class SearcherActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[SearcherActor])

    override def receive: Receive = {

        case SearchRequest(query,page,size) => {
            log.info("Received SearchRequest('{}',{},{})", query, page, size)

            val originalSender = sender
            context.actorOf(Props(new Actor() {
                def receive = LoggingReceive {
                    case IndexResultsFound(query: String, results: ResultWrapperDTO) => {
                        log.info("Received " + results.getTotalHits + " results from index for query '" + query + "'")

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
                }

                def sendResponseAndShutdown(response: Any) = {
                    originalSender ! response
                    log.debug("Stopping context capturing actor")
                    context.stop(self)
                }

                log.info("Sending SearchIndex('{}',{},{}) message", query, page, size)

                indexStore ! SearchIndex(query, page, size)

                import context.dispatcher
                val timeoutMessager = context.system.scheduler.
                    scheduleOnce(5 seconds) {
                        self ! IndexRetrievalTimeout
                    }
            }))
        }

    }
}
