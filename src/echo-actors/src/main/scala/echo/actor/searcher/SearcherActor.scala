package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.protocol.Protocol._
import echo.core.dto.document.Document

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class SearcherActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[SearcherActor])

    override def receive: Receive = {

        case SearchRequest(query: String) => {
            log.info("Received SearchRequest for query: " + query)

            // TODO for now, we will pass the query 1:1 to the indexRepo; later we will have to do some query processing and additional scoring/data aggregation (show-images, etc)
            implicit val timeout = Timeout(5 seconds)

            log.info("Sending SearchIndex('"+query+"') message")
            val future = indexStore ? SearchIndex(query)
            val response = Await.result(future, timeout.duration).asInstanceOf[IndexResult] // TODO hier habe ich als typ IndexMessage, muss ggf ans neue protocoll angepasst werden
            response match {

                case IndexResultsFound(query: String, results: Array[Document]) => {
                    log.info("Received " + results.length + " results from index for query '" + query + "'")
                    sender ! SearchResults(results)
                }

                case NoIndexResultsFound(query: String) => {
                    log.info("Received NO results from index for query '" + query + "'")
                    sender ! SearchResults(Array.empty)
                }
            }
        }

    }
}
