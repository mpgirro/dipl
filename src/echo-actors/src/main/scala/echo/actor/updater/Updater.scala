package echo.actor.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogProtocol.ProposeNewFeed

/**
  * @author Maximilian Irro
  */

object Updater {
    final val name = "updater"
    def props(): Props = Props(new Updater()).withDispatcher("echo.updater.dispatcher")
}

class Updater extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    //private val benchmarkFeedMap = new Map[String, String]

    private var catalog: ActorRef = _
    private var crawler: ActorRef = _

    override def receive: Receive = {

        case ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogActor(_)")
            catalog = ref

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ProposeNewFeed(url, rtt) =>
            catalog ! ProposeNewFeed(url, rtt.bumpRTTs())

        case ProcessFeed(exo, url, job: FetchJob, rtt) =>
            crawler ! DownloadWithHeadCheck(exo, url, job, rtt.bumpRTTs())
    }

}
