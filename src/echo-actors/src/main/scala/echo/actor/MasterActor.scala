package echo.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.crawler.CrawlerActor
import echo.actor.directory.DirectoryStore
import echo.actor.gateway.GatewayActor
import echo.actor.index.IndexStore
import echo.actor.parser.ParserActor
import ActorProtocol._
import echo.actor.searcher.SearcherActor
import echo.core.util.DocumentFormatter

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class MasterActor extends Actor with ActorLogging {

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    implicit val internalTimeout = Timeout(5 seconds)

    override def preStart(): Unit = {
        val indexStore = context.watch(context.actorOf(Props[IndexStore]
            .withDispatcher("echo.index-store.dispatcher"),
            name = "indexStore"))
        val parser = context.watch(context.actorOf(Props[ParserActor]
            .withDispatcher("echo.parser.dispatcher"),
            name = "parser"))
        val searcher = context.watch(context.actorOf(Props[SearcherActor], name = "searcher"))
        val crawler = context.watch(context.actorOf(Props[CrawlerActor]
            .withDispatcher("echo.crawler.dispatcher"),
            name = "crawler"))
        val directoryStore = context.watch(context.actorOf(Props[DirectoryStore]
            .withDispatcher("echo.directory.dispatcher"),
            name = "directoryStore"))
        val gateway = context.watch(context.actorOf(Props[GatewayActor], name = "gateway"))

        val cli = context.watch(context.actorOf(Props(new CliActor(indexStore, parser, searcher, crawler, directoryStore, gateway))
            .withDispatcher("echo.cli.dispatcher"),
            name = "cli"))

        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefDirectoryStoreActor(directoryStore)

        parser ! ActorRefIndexStoreActor(indexStore)
        parser ! ActorRefDirectoryStoreActor(directoryStore)
        parser ! ActorRefCrawlerActor(crawler)

        searcher ! ActorRefIndexStoreActor(indexStore)

        gateway ! ActorRefSearcherActor(searcher)
        gateway ! ActorRefDirectoryStoreActor(directoryStore)

        directoryStore ! ActorRefCrawlerActor(crawler)
        directoryStore ! ActorRefIndexStoreActor(indexStore)

        /*
        cli ! ActorRefIndexStoreActor(indexStore)
        cli ! ActorRefParserActor(parser)
        cli ! ActorRefSearcherActor(searcher)
        cli ! ActorRefCrawlerActor(crawler)
        cli ! ActorRefDirectoryStoreActor(directoryStore)
        cli ! ActorRefGatewayActor(gateway)
        */

        log.info("EchoMaster up and running")
    }

    override def receive: Receive = {
        case Terminated(actor) => onTerminated(actor)
    }

    private def onTerminated(actor: ActorRef): Unit = {
        log.error("Terminating the system because {} terminated!", actor)
        context.system.terminate()
    }

}
