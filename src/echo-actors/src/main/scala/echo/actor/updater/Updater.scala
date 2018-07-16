package echo.actor.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogBroker
import echo.actor.catalog.CatalogProtocol.{CatalogCommand, ProposeNewFeed}
import echo.core.benchmark.RoundTripTime

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

        case ProposeNewFeed(url, rtts) =>
            catalog ! ProposeNewFeed(url, RoundTripTime.appendNow(rtts))

        case ProcessFeed(exo, url, job: FetchJob, rtts) =>
            crawler ! DownloadWithHeadCheck(exo, url, job, RoundTripTime.appendNow(rtts))
    }

}
