package echo.actor

import java.io.{FileWriter, PrintWriter}
import java.sql.Timestamp
import java.text.SimpleDateFormat

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogBroker
import echo.actor.cli.CLI
import echo.actor.crawler.Crawler
import echo.actor.gateway.Gateway
import echo.actor.index.IndexBroker
import echo.actor.parser.Parser
import echo.actor.searcher.{Searcher, SearcherWorker}
import echo.actor.updater.Updater
import echo.core.benchmark._

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object NodeMaster {
    final val name = "node"
    def props(): Props = Props(new NodeMaster())
}

class NodeMaster extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    private implicit val executionContext = context.system.dispatcher

    private val cluster = Cluster(context.system)

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private var index: ActorRef = _
    private var parser: ActorRef = _
    private var searcher: ActorRef = _
    private var crawler: ActorRef = _
    private var catalog: ActorRef = _
    private var gateway: ActorRef = _
    private var updater: ActorRef = _
    private var cli: ActorRef = _

    private val rttMonitor = new RoundTripTimeMonitor(ArchitectureType.ECHO_AKKA)
    private val mpsMonitor = new MessagesPerSecondMonitor(ArchitectureType.ECHO_AKKA, 46) // 'cause we have 21 actors in place that will report their MPS

    private val benchmarkUtil = new BenchmarkUtil("../benchmark/")

    override def preStart(): Unit = {

        val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

        index    = context.watch(context.actorOf(IndexBroker.props(),   IndexBroker.name))
        parser   = context.watch(context.actorOf(Parser.props(),        Parser.name(1)))
        searcher = context.watch(context.actorOf(Searcher.props(),      Searcher.name(1)))
        crawler  = context.watch(context.actorOf(Crawler.props(),       Crawler.name(1)))
        catalog  = context.watch(context.actorOf(CatalogBroker.props(), CatalogBroker.name))
        gateway  = context.watch(context.actorOf(Gateway.props(),       Gateway.name(1)))
        updater  = context.watch(context.actorOf(Updater.props(),       Updater.name))

        createCLI()

        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefCatalogStoreActor(catalog)

        parser ! ActorRefCatalogStoreActor(catalog)
        parser ! ActorRefCrawlerActor(crawler)

        searcher ! ActorRefIndexStoreActor(index)

        gateway ! ActorRefCatalogStoreActor(catalog)
        gateway ! ActorRefSearcherActor(searcher)

        catalog ! ActorRefCrawlerActor(crawler)
        catalog ! ActorRefCatalogStoreActor(catalog)
        catalog ! ActorRefUpdaterActor(updater)

        updater ! ActorRefCatalogStoreActor(catalog)
        updater ! ActorRefCrawlerActor(crawler)

        // send for benchmark monitor
        catalog  ! ActorRefBenchmarkMonitor(self)
        index    ! ActorRefBenchmarkMonitor(self)
        crawler  ! ActorRefBenchmarkMonitor(self)
        parser   ! ActorRefBenchmarkMonitor(self)
        searcher ! ActorRefBenchmarkMonitor(self)
        updater  ! ActorRefBenchmarkMonitor(self)
        gateway  ! ActorRefBenchmarkMonitor(self)

        log.info("up and running")
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            sendStartMessagePerSecondMonitoringMessages()

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            sendStopMessagePerSecondMonitoringMessages()

        case MessagePerSecondReport(name, mps) =>
            log.debug("Received StopMessagePerSecondMonitoring({},{})", name, mps)
            mpsMonitor.addMetric(name, mps)
            if (mpsMonitor.isFinished) {
                log.info("MPS reporting finished; results in CSV format :")
                println(mpsMonitor.toCsv)
            }

        case MonitorFeedProgress(feedProperties) =>
            log.debug("Received MonitorFeedProgress(_)")
            rttMonitor.initWithProperties(feedProperties)
            mpsMonitor.reset()

        case MonitorQueryProgress(queries) =>
            log.debug("Received MonitorQueryProgress(_)")
            rttMonitor.initWithQueries(queries)
            mpsMonitor.reset()

        case IndexSubSystemRoundTripTimeReport(rtt) =>
            log.debug("Received IndexSubSystemRoundTripTimeReport(_)", rtt)
            addRttReport(rtt)

        case RetrievalSubSystemRoundTripTimeReport(rtt) =>
            log.debug("Received RetrievalSubSystemRoundTripTimeReport(_)")
            addRttReport(rtt)

        case Terminated(corpse) => onTerminated(corpse)

        case ShutdownSystem()   => onSystemShutdown()
    }

    private def onTerminated(corpse: ActorRef): Unit = {
        if (corpse == cli) {
            createCLI() // we simply re-create the CLI
        } else {
            log.error("Oh noh! A critical subsystem died : {}", corpse.path)
            self ! ShutdownSystem()
        }
    }

    private def onSystemShutdown(): Unit = {
        log.info("initiating shutdown sequence")

        // it is important to shutdown all actor(supervisor) befor shutting down the actor system
        context.system.stop(cli)
        context.system.stop(crawler)    // these have a too full inbox usually to let them finish processing
        context.system.stop(catalog)
        context.system.stop(gateway)
        context.system.stop(index)
        context.system.stop(parser)
        context.system.stop(searcher)

        cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

        context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
        //context.stop(self)  // master

    }

    private def createCLI(): Unit = {
        cli = context.actorOf(CLI.props(self, parser, searcher, crawler, catalog, gateway, updater, self),
            name = CLI.name)
        context watch cli
    }

    private def addRttReport(rtt: RoundTripTime): Unit = {
        rttMonitor.addRoundTripTime(rtt)
        if (rttMonitor.isFinished) {
            sendStopMessagePerSecondMonitoringMessages()
            //rttMonitor.getAllRTTs.stream().forEach(rtt => log.info(rtt.getRtts.toString))

            val size = rttMonitor.getAllRTTs.size()

            var progressFile = "akka-rtt-progress-not-set.txt"
            var overallFile = "akka-rtt-overall-not-set.txt"
            if (Workflow.PODCAST_INDEX == rttMonitor.getWorkflow || Workflow.EPISODE_INDEX == rttMonitor.getWorkflow ) {
                progressFile = "akka-index"+size+"-rtt-progress"
                overallFile  = "akka-index-rtt-overall"
            } else if (Workflow.RESULT_RETRIEVAL == rttMonitor.getWorkflow) {
                progressFile = "akka-search"+size+"-rtt-progress"
                overallFile  = "akka-search-rtt-overall"
            } else {
                log.warning("Unhandled Workflow : {}", rttMonitor.getWorkflow)
            }

            //log.info("RTT progress CSV :")
            val progressCSV = rttMonitor.getProgressCSV
            benchmarkUtil.writeToFile(progressFile, progressCSV)

            //log.info("RTT overall CSV :")
            val overallCSV = rttMonitor.getOverallCSV
            benchmarkUtil.appendToFile(overallFile, overallCSV)
        }
    }

    private def sendStartMessagePerSecondMonitoringMessages(): Unit = {
        catalog  ! StartMessagePerSecondMonitoring
        index    ! StartMessagePerSecondMonitoring
        crawler  ! StartMessagePerSecondMonitoring
        parser   ! StartMessagePerSecondMonitoring
        searcher ! StartMessagePerSecondMonitoring
        updater  ! StartMessagePerSecondMonitoring
        gateway  ! StartMessagePerSecondMonitoring
    }

    private def sendStopMessagePerSecondMonitoringMessages(): Unit = {
        catalog  ! StopMessagePerSecondMonitoring
        index    ! StopMessagePerSecondMonitoring
        crawler  ! StopMessagePerSecondMonitoring
        parser   ! StopMessagePerSecondMonitoring
        searcher ! StopMessagePerSecondMonitoring
        updater  ! StopMessagePerSecondMonitoring
        gateway  ! StopMessagePerSecondMonitoring
    }

}
