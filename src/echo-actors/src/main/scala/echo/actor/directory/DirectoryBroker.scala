package echo.actor.directory

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefCrawlerActor, ActorRefIndexStoreActor}

/**
  * @author Maximilian Irro
  */
class DirectoryBroker extends Actor with ActorLogging {

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = 1 // Option(CONFIG.getInt("echo.directory.store-count")).getOrElse(1) // TODO

    private var currentStoreIndex = 1

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(STORE_COUNT) {
            val directoryStore = createDirectoryStore()
            context watch directoryStore
            ActorRefRoutee(directoryStore)
        }
        Router(BroadcastRoutingLogic(), routees)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(ActorRefCrawlerActor(crawler), sender()))

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(ActorRefIndexStoreActor(indexStore), sender()))

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            router = router.removeRoutee(corpse)
            if(router.routees.isEmpty) {
                log.error("Broker shutting down due to no more stores available")
                context.stop(self)
            }

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())

    }

    private def createDirectoryStore(): ActorRef = {
        val storeIndex = currentStoreIndex
        val directoryStore = context.actorOf(Props(new DirectorySupervisor())
            .withDispatcher("echo.directory.dispatcher"),
            name = "store-" + storeIndex)
        currentStoreIndex += 1

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => directoryStore ! ActorRefCrawlerActor(c) )
        Option(indexStore).foreach(i => directoryStore ! ActorRefIndexStoreActor(i))

        directoryStore
    }

}
