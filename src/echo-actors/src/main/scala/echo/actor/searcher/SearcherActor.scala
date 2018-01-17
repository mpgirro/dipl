package echo.actor.searcher

import java.util.concurrent.TimeoutException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.protocol.ActorMessages._
import echo.core.dto.document.{DTO, ResultWrapperDTO}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class SearcherActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[SearcherActor])

    override def receive: Receive = {

        case SearchRequest(query,page,size) => {
            log.info("Received SearchRequest('{}',{},{})", query, page, size)

            // TODO for now, we will pass the query 1:1 to the indexRepo; later we will have to do some query processing and additional scoring/data aggregation (show-images, etc)
            implicit val timeout = Timeout(10 seconds)

            log.info("Sending SearchIndex('{}',{},{}) message", query, page, size)
            val future = indexStore ? SearchIndex(query, page, size)
            try{
                val response = Await.result(future, timeout.duration).asInstanceOf[IndexResult]
                response match {

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

                        sender ! SearchResults(results)
                    }

                    case NoIndexResultsFound(query: String) => {
                        log.info("Received NO results from index for query '" + query + "'")
                        sender ! SearchResults(new ResultWrapperDTO)
                    }
                }
            } catch {
                case e: TimeoutException => {
                    log.error("Timeout waiting for answer from indexStore")
                    //self.forward(SearchRequest(query))
                }
            }


        }

    }
}
