package echo.actor.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogProtocol.ProposeNewFeed
import echo.core.benchmark.mps.MessagesPerSecondMeter

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

    private val mpsMeter = new MessagesPerSecondMeter()

    private var expectedMsgCount = 0
    private var expectedMsgGoal = 0

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

        case ExpectedMessages(count) =>
            log.debug("Received ExpectedMessages({})", count)
            expectedMsgCount = 0
            expectedMsgGoal = count

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMeter.startMeasurement()

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            if (mpsMeter.isMeasuring) {
                mpsMeter.stopMeasurement()
                benchmarkMonitor ! MessagePerSecondReport(self.path.toString, mpsMeter.getResult.mps)
            }

        case ProposeNewFeed(url, rtt) =>
            tickMpsMeter()
            catalog ! ProposeNewFeed(url, rtt.bumpRTTs())

        case ProcessFeed(exo, url, job: FetchJob, rtt) =>
            tickMpsMeter()
            crawler ! DownloadWithHeadCheck(exo, url, job, rtt.bumpRTTs())
    }

    private def tickMpsMeter(): Unit = {
        mpsMeter.incrementCounter()
        expectedMsgCount += 1
        if (expectedMsgCount == expectedMsgGoal && mpsMeter.isMeasuring) {
            mpsMeter.stopMeasurement()
            benchmarkMonitor ! MessagePerSecondReport(self.path.toString, mpsMeter.getResult.mps)
        }
    }

}
