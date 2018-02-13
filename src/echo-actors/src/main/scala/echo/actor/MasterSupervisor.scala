package echo.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.util.Timeout
import echo.actor.ActorProtocol._
import echo.actor.cli.CliActor
import echo.actor.crawler.CrawlerSupervisor
import echo.actor.directory.DirectorySupervisor
import echo.actor.gateway.GatewayActor
import echo.actor.index.IndexStore
import echo.actor.parser.ParserActor
import echo.actor.searcher.SearcherActor

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class MasterSupervisor extends Actor with ActorLogging {

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    implicit val internalTimeout = Timeout(5 seconds)

    private var index: ActorRef = _
    private var parser: ActorRef = _
    private var searcher: ActorRef = _
    private var crawler: ActorRef = _
    private var directory: ActorRef = _
    private var gateway: ActorRef = _
    private var cli: ActorRef = _

    override def preStart(): Unit = {
        index = context.watch(context.actorOf(Props[IndexStore]
            .withDispatcher("echo.index.dispatcher"),
            name = "index"))
        parser = context.watch(context.actorOf(Props[ParserActor]
            .withDispatcher("echo.parser.dispatcher"),
            name = "parser"))
        searcher = context.watch(context.actorOf(Props[SearcherActor]
            .withDispatcher("echo.searcher.dispatcher"),
            name = "searcher"))
        /*
        val crawler = context.watch(context.actorOf(Props[CrawlerActor]
            .withDispatcher("echo.crawler.dispatcher"),
            name = "crawler"))
        */
        // TODO brauchen nicht die supervisor den eigenen threadpool? sodass sich maximal children gegenseitig blockieren?
        crawler = context.actorOf(Props[CrawlerSupervisor], name = "crawler-supervisor")
        context watch crawler

        /*
        val directoryStore = context.watch(context.actorOf(Props[DirectoryStore]
            .withDispatcher("echo.directory.dispatcher"),
            name = "directoryStore"))
        */
        // TODO brauchen nicht die supervisor den eigenen threadpool? sodass sich maximal children gegenseitig blockieren?
        directory = context.actorOf(Props[DirectorySupervisor], name = "directory-supervisor")
        context watch directory

        gateway = context.watch(context.actorOf(Props[GatewayActor]
            .withDispatcher("echo.gateway.dispatcher"),
            name = "gateway"))

        cli = context.watch(context.actorOf(Props(new CliActor(self, index, parser, searcher, crawler, directory, gateway))
            .withDispatcher("echo.cli.dispatcher"),
            name = "cli"))

        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefDirectoryStoreActor(directory)
        crawler ! ActorRefIndexStoreActor(index)

        parser ! ActorRefIndexStoreActor(index)
        parser ! ActorRefDirectoryStoreActor(directory)
        parser ! ActorRefCrawlerActor(crawler)

        searcher ! ActorRefIndexStoreActor(index)

        gateway ! ActorRefSearcherActor(searcher)
        gateway ! ActorRefDirectoryStoreActor(directory)

        directory ! ActorRefCrawlerActor(crawler)
        directory ! ActorRefIndexStoreActor(index)

        log.info("Echo:Master up and running")
    }

    override def postStop: Unit = {
        log.info(s"${self.path.name} shut down")
    }

    override def receive: Receive = {
        case Terminated(actor) => onTerminated(actor)
        case ShutdownSystem()  => onSystemShutdown()
    }

    private def onTerminated(actor: ActorRef): Unit = {
        log.error("Oh noh! A subsystem critically failed : {}", actor)
    }

    private def onSystemShutdown(): Unit = {
        log.info("Received ShutdownSystem")

        // it is important to shutdown all actor(supervisor) befor shutting down the actor system
        // otherwise Akka HTTP might block ports etc
        context.system.stop(crawler)
        context.system.stop(gateway)
        context.system.stop(index)
        context.system.stop(directory)
        context.system.stop(parser)
        context.system.stop(searcher)
        context.system.stop(cli)

        // now its safe to shutdown
        context.system.terminate()
    }

}
