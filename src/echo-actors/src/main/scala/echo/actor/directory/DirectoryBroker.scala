package echo.actor.directory

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefDirectoryStoreActor}
import echo.actor.directory.DirectoryProtocol.{DirectoryCommand, DirectoryQuery}

/**
  * @author Maximilian Irro
  */

object DirectoryBroker {
    final val name = "directory"
    def props(): Props = Props(new DirectoryBroker())
}

class DirectoryBroker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = Option(CONFIG.getInt("echo.directory.store-count")).getOrElse(1) // TODO
    private val DATABASE_URLs = Array("jdbc:h2:mem:echo1", "jdbc:h2:mem:echo2")// TODO I'll have to thing about a better solution in a distributed context

    private var crawler: ActorRef = _

    /*
     * We define two separate routings, based on the Command–query separation principle
     * - Command messages (create/update/delete) are PubSub sent to all stores
     * - Query messages (read) are sent to one store
     */
    private var commandRouter: Router = _
    private var queryRouter: Router = _ ;
    {
        val routees: Vector[ActorRefRoutee] = (1 to List(STORE_COUNT, DATABASE_URLs.length).min)
            .map(i => {
                val databaseUrl = DATABASE_URLs(i-1)
                val directoryStore = createDirectoryStore(i, databaseUrl)
                context watch directoryStore
                ActorRefRoutee(directoryStore)
            })
            .to[Vector]

        commandRouter = Router(BroadcastRoutingLogic(), routees)
        queryRouter = Router(RoundRobinRoutingLogic(), routees)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case msg @ ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            commandRouter.route(msg, sender())

        case msg @ ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            commandRouter.route(msg, sender())

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

    private def createDirectoryStore(storeIndex: Int, databaseUrl: String): ActorRef = {
        val directoryStore = context.actorOf(DirectoryStore.props(databaseUrl),
            name = DirectoryStore.name(storeIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => directoryStore ! ActorRefCrawlerActor(c) )

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
