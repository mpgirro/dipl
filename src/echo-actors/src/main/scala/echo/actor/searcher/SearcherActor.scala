package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._

import scala.language.postfixOps

class SearcherActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = ConfigFactory.load().getInt("echo.gateway.default-page")
    private val DEFAULT_SIZE: Int = ConfigFactory.load().getInt("echo.gateway.default-size")

    private var indexStore: ActorRef = _

    private var responseHandlerCounter = 0

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor")
            indexStore = ref

        case SearchRequest(query, page, size) =>
            log.debug("Received SearchRequest('{}',{},{})", query, page, size)

            // TODO do some query processing (like extracting "sort:date:asc" and "sort:date:desc")

            val p: Int = page match {
                case Some(x) => x
                case None    => DEFAULT_PAGE
            }
            val s: Int = size match {
                case Some(x) => x
                case None    => DEFAULT_SIZE
            }

            val originalSender = Some(sender) // this is important to not expose the handler

            responseHandlerCounter += 1
            val handler = context.actorOf(IndexStoreReponseHandler.props(indexStore, originalSender), s"handler-${responseHandlerCounter}")

            indexStore.tell(SearchIndex(query, p, s), handler)

            log.debug("Finished SearchRequest('{}',{},{})", query, page, size)

    }
}
