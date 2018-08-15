package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.benchmark.mps.MessagesPerSecondMeter

/**
  * @author Maximilian Irro
  */

object Searcher {
    def name(nodeIndex: Int): String = "searcher-" + nodeIndex
    def props(): Props = Props(new Searcher())
}

class Searcher extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.searcher.worker-count")).getOrElse(5)

    private var workerIndex = 0

    private var indexStore: ActorRef = _
    private var benchmarkMonitor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter()

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val parser = createWorkerActor()
            context watch parser
            ActorRefRoutee(parser)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def receive: Receive = {

        case msg @ ActorRefIndexStoreActor(ref) =>
            log.debug("ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefCLIActor(_)")
            benchmarkMonitor = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMeter.startMeasurement()
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            mpsMeter.stopMeasurement()
            benchmarkMonitor ! MessagePerSecondReport(self.path.toString, mpsMeter.getResult.mps)
            router.routees.foreach(r => r.send(msg, sender()))

        case request: SearchRequest =>
            mpsMeter.registerMessage()
            router.route(request, sender())

        case work =>
            log.warning("Routing work of UNKNOWN kind : {}", work.getClass)
            mpsMeter.registerMessage()
            router.route(work, sender())
    }


    private def createWorkerActor(): ActorRef = {
        workerIndex += 1
        val worker = context.actorOf(DelegationSearcherWorker.props(), DelegationSearcherWorker.name(workerIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(indexStore).foreach(d => worker ! ActorRefIndexStoreActor(d))

        worker
    }

}
