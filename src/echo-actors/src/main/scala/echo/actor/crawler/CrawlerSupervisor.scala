package echo.actor.crawler

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import echo.actor.ActorProtocol.{ActorRefDirectoryStoreActor, ActorRefIndexStoreActor, ActorRefParserActor}

/**
  * @author Maximilian Irro
  */
class CrawlerSupervisor extends Actor with ActorLogging {

    private val WORKER_COUNT = 1 // TODO read this from config
    private var workerIndex = 1

    private var parser: ActorRef = _
    private var directory: ActorRef = _
    private var indexStore: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val crawler = createCrawler()
            context watch crawler
            ActorRefRoutee(crawler)
        }
        Router(RoundRobinRoutingLogic(), routees) // TODO hier gibt es vll einen besseren router als roundrobin. balanced mailbox?
    }

    override def postStop: Unit = {
        log.info(s"${self.path.name} shut down")
    }

    override def receive: Receive = {
        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref
            router.routees.foreach(r => r.send(ActorRefParserActor(parser), sender()))

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directory = ref
            router.routees.foreach(r => r.send(ActorRefDirectoryStoreActor(directory), sender()))

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(ActorRefIndexStoreActor(indexStore), sender()))

        case Terminated(a) =>
            router = router.removeRoutee(a)
            val crawler = createCrawler()
            context watch crawler
            router = router.addRoutee(crawler)

        case work =>
            router.route(work, sender())
    }

    private def createCrawler(): ActorRef = {
        val crawler = context.actorOf(Props[CrawlerActor]
            .withDispatcher("echo.crawler.dispatcher"),
            name = "crawler-" + workerIndex)

        workerIndex += 1

        Option(parser).map(p => directory ! ActorRefParserActor(p) )
        Option(directory).map(d => directory ! ActorRefDirectoryStoreActor(d))

        crawler
    }

}
