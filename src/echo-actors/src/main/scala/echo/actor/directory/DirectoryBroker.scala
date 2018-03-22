package echo.actor.directory

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefIndexStoreActor}
import echo.actor.directory.DirectoryProtocol.{DirectoryCommand, DirectoryQuery}

/**
  * @author Maximilian Irro
  */
class DirectoryBroker extends Actor with ActorLogging {

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = 1 // Option(CONFIG.getInt("echo.directory.store-count")).getOrElse(1) // TODO

    private var currentStoreIndex = 1

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    /*
     * We define two separate routings, based on the Command–query separation principle
     * - Command messages (create/update/delete) are PubSub sent to all stores
     * - Query messages (read) are sent to one store
     */
    private var commandRouter: Router = _
    private var queryRouter: Router = _ ;
    {
        val routees: Vector[ActorRefRoutee] = Vector.fill(STORE_COUNT) {
            val directoryStore = createDirectoryStore()
            context watch directoryStore
            ActorRefRoutee(directoryStore)
        }
        commandRouter = Router(BroadcastRoutingLogic(), routees)
        queryRouter = Router(RoundRobinRoutingLogic(), routees)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            //commandRouter.routees.foreach(r => r.send(ActorRefCrawlerActor(crawler), sender()))
            commandRouter.route(ActorRefCrawlerActor(crawler), sender())

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            //commandRouter.routees.foreach(r => r.send(ActorRefIndexStoreActor(indexStore), sender()))
            commandRouter.route(ActorRefIndexStoreActor(indexStore), sender())

        case command: DirectoryCommand =>
            log.debug("Routing command: {}", command.getClass)
            commandRouter.route(command, sender())

        case query: DirectoryQuery =>
            log.debug("Routing query : {}", query.getClass)
            queryRouter.route(query, sender())

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            removeStore(corpse)

        case message =>
            log.warning("Routing GENERAL message of kind (assuming it should be broadcast) : {}", message.getClass)
            commandRouter.route(message, sender())
    }

    private def createDirectoryStore(): ActorRef = {
        val storeIndex = currentStoreIndex
        val directoryStore = context.actorOf(Props(new DirectoryStore())
            .withDispatcher("echo.directory.dispatcher"),
            name = "store-" + storeIndex)
        currentStoreIndex += 1

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => directoryStore ! ActorRefCrawlerActor(c) )
        Option(indexStore).foreach(i => directoryStore ! ActorRefIndexStoreActor(i))

        directoryStore
    }

    private def removeStore(routee: ActorRef): Unit = {
        commandRouter = commandRouter.removeRoutee(routee)
        queryRouter = queryRouter.removeRoutee(routee)
        if (commandRouter.routees.isEmpty || queryRouter.routees.isEmpty) {
            log.error("Broker shutting down due to no more stores available")
            context.stop(self)
        }
    }

}
