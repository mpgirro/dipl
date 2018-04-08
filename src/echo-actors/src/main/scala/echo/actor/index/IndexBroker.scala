package echo.actor.index

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.index.IndexProtocol.{IndexEvent, IndexQuery}
import echo.core.exception.SearchException

import scala.concurrent.duration._

/**
  * @author Maximilian Irro
  */
object IndexBroker {
    final val name = "index"
    def props(): Props = Props(new IndexBroker())
}

class IndexBroker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = Option(CONFIG.getInt("echo.index.store-count")).getOrElse(1) // TODO
    private val INDEX_PATHs = Array("/Users/max/volumes/echo/index_1", "/Users/max/volumes/echo/index_2") // TODO I'll have to thing about a better solution in a distributed context
    private val CREATE_INDEX: Boolean = Option(CONFIG.getBoolean("echo.index.create-index")).getOrElse(false)

    /*
     * We define two separate routings, based on the Command–query separation principle
     * - Command messages (create/update/delete) are PubSub sent to all stores
     * - Query messages (read) are sent to one store
     */
    private var commandRouter: Router = _ ;
    private var queryRouter: Router = _ ;
    {
        // TODO 1 to List(STORE_COUNT, INDEX_PATHs.length).min
        val routees: Vector[ActorRefRoutee] = (1 to List(STORE_COUNT, INDEX_PATHs.length).min)
            .map(i => {
                val indexPath = INDEX_PATHs(i-1)
                val indexStore = createIndexStore(i, indexPath)
                context watch indexStore
                ActorRefRoutee(indexStore)
            })
            .to[Vector]

        commandRouter = Router(BroadcastRoutingLogic(), routees)
        queryRouter = Router(RoundRobinRoutingLogic(), routees)
    }

    // TODO is this working when running in a cluster setup?
    override val supervisorStrategy: SupervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
            case _: SearchException => Resume
            case _: Exception       => Escalate
        }

    override def receive: Receive = {

        case command: IndexEvent =>
            log.debug("Routing command: {}", command.getClass)
            commandRouter.route(command, sender())

        case query: IndexQuery =>
            log.debug("Routing query : {}", query.getClass)
            queryRouter.route(query, sender())

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            removeStore(corpse)

        case message =>
            log.warning("Routing GENERAL message of kind (assuming it should be broadcast) : {}", message.getClass)
            commandRouter.route(message, sender())

    }

    private def createIndexStore(storeIndex: Int, indexPath: String): ActorRef = {
        val index = context.watch(context.actorOf(Props(new IndexStore(indexPath, CREATE_INDEX))
            .withDispatcher("echo.index.dispatcher"),
            name = "store-" + storeIndex))

        index
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
