package echo.actor.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogProtocol.ProposeNewFeed
import echo.core.benchmark.MessagesPerSecondCounter

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
    private var benchmarkMonitor: ActorRef = _

    private val mpsCounter = new MessagesPerSecondCounter()

    override def receive: Receive = {

        case ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogActor(_)")
            catalog = ref

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

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

        case ProposeNewFeed(url, rtt) =>
            mpsCounter.incrementCounter()
            catalog ! ProposeNewFeed(url, rtt.bumpRTTs())

        case ProcessFeed(exo, url, job: FetchJob, rtt) =>
            mpsCounter.incrementCounter()
            crawler ! DownloadWithHeadCheck(exo, url, job, rtt.bumpRTTs())
    }

}
