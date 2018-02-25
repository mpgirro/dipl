package echo.actor.parser

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefDirectoryStoreActor, ActorRefIndexStoreActor}

/**
  * @author Maximilian Irro
  */
class ParserSupervisor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.parser.worker-count")).getOrElse(2)

    private var workerIndex = 1

    private var indexStore: ActorRef = _
    private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val parser = createParserActor()
            context watch parser
            ActorRefRoutee(parser)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(ActorRefIndexStoreActor(indexStore), sender()))

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref
            router.routees.foreach(r => r.send(ActorRefDirectoryStoreActor(directoryStore), sender()))

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(ActorRefCrawlerActor(crawler), sender()))

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())
    }

    private def createParserActor(): ActorRef = {
        val parser = context.actorOf(Props[ParserActor]
            .withDispatcher("echo.parser.dispatcher"),
            name = "worker-" + workerIndex)

        workerIndex += 1

        // forward the actor refs to the worker, but only if those references haven't died
        Option(indexStore).foreach(i => parser ! ActorRefIndexStoreActor(i) )
        Option(directoryStore).foreach(d => parser ! ActorRefDirectoryStoreActor(d))
        Option(crawler).foreach(c => parser ! ActorRefCrawlerActor(c))

        parser
    }

}
