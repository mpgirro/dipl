package echo.actor.parser

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.exception.FeedParsingException

import scala.concurrent.duration._

/**
  * @author Maximilian Irro
  */

object Parser {
    def name(nodeIndex: Int): String = "parser-" + nodeIndex
    def props(): Props = Props(new Parser())
}

class Parser extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.parser.worker-count")).getOrElse(2)

    private var workerIndex = 0

    private var catalogStore: ActorRef = _
    private var crawler: ActorRef = _
    private var benchmarkMonitor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter()

    private var expectedMsgCount = 0
    private var expectedMsgGoal = 0

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val parser = createParserActor()
            context watch parser
            ActorRefRoutee(parser)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override val supervisorStrategy: SupervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
            case _: FeedParsingException => Resume
            case _: Exception            => Escalate
        }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case msg @ ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogStoreActor(_)")
            catalogStore = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefCLIActor(_)")
            benchmarkMonitor = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case ExpectedMessages(count) =>
            log.debug("Received ExpectedMessages({})", count)
            expectedMsgCount = 0
            expectedMsgGoal = count

        case msg @ StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMeter.startMeasurement()
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            reportMps()

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            tickMpsMeter()
            router.route(work, sender())
    }

    private def createParserActor(): ActorRef = {
        workerIndex += 1
        val parser = context.actorOf(ParserWorker.props(), ParserWorker.name(workerIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(catalogStore).foreach(d => parser ! ActorRefCatalogStoreActor(d))
        Option(crawler).foreach(c => parser ! ActorRefCrawlerActor(c))

        parser
    }

    private def tickMpsMeter(): Unit = {
        mpsMeter.incrementCounter()
        expectedMsgCount += 1
        if (expectedMsgCount == expectedMsgGoal && mpsMeter.isMeasuring) {
            reportMps()
        }
    }

    private def reportMps(): Unit = {
        if (mpsMeter.isMeasuring) {
            mpsMeter.stopMeasurement()
            benchmarkMonitor ! MessagePerSecondReport(self.path.toString, mpsMeter.getResult.mps)
            router.routees.foreach(r => r.send(StopMessagePerSecondMonitoring, sender()))
        }
    }

}
