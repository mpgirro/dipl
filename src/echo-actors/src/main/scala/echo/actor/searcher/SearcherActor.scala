package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.protocol.ActorMessages._

import scala.language.postfixOps

class SearcherActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[SearcherActor])

    override def receive: Receive = {

        case SearchRequest(query,page,size) => {
            log.info("Received SearchRequest('{}',{},{})", query, page, size)

            val originalSender = Some(sender) // this is important to not expose the handler
            val handler = context.actorOf(IndexStoreReponseHandler.props(indexStore, originalSender), "searcher-cameo-message-handler")
            indexStore.tell(SearchIndex(query, page, size), handler)
        }

    }
}
