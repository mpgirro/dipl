package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.domain.dto.ResultWrapperDTO

import scala.concurrent.duration._
import scala.language.postfixOps

class SearcherActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    // TODO these values are used by searcher and gateway, so save them somewhere more common for both
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)
    private val INTERNAL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private var indexStore: ActorRef = _

    private var responseHandlerCounter = 0

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
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

            if (p < 0) {
                sender ! SearchResults(new ResultWrapperDTO)
            }

            if (s < 0) {
                sender ! SearchResults(new ResultWrapperDTO)
            }

            val originalSender = Some(sender) // this is important to not expose the handler

            responseHandlerCounter += 1
            val handler = context.actorOf(IndexStoreReponseHandler.props(indexStore, originalSender, INTERNAL_TIMEOUT), s"handler-${responseHandlerCounter}")

            indexStore.tell(SearchIndex(query, p, s), handler)

            log.debug("Finished SearchRequest('{}',{},{})", query, page, size)

    }
}
