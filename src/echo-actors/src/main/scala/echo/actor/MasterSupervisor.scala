package echo.actor

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, SupervisorStrategy, Terminated}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.cli.CliActor
import echo.actor.crawler.CrawlerSupervisor
import echo.actor.directory.{DirectoryBroker, DirectoryStore}
import echo.actor.gateway.GatewayActor
import echo.actor.index.{IndexBroker, IndexStore}
import echo.actor.parser.{ParserActor, ParserSupervisor}
import echo.actor.searcher.SearcherActor

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Success

/**
  * @author Maximilian Irro
  */
class MasterSupervisor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    private implicit val executionContext = context.system.dispatcher

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private var index: ActorRef = _
    private var parser: ActorRef = _
    private var searcher: ActorRef = _
    private var crawler: ActorRef = _
    private var directory: ActorRef = _
    private var gateway: ActorRef = _
    private var cli: ActorRef = _

    override def preStart(): Unit = {

        index = context.watch(context.actorOf(Props[IndexBroker]
            .withDispatcher("echo.index.dispatcher"),
            name = "index"))

        /*
        parser = context.watch(context.actorOf(Props[ParserActor]
            .withDispatcher("echo.parser.dispatcher"),
            name = "parser"))
            */
        parser = context.actorOf(Props[ParserSupervisor], name = "parser")
        context watch parser

        searcher = context.watch(context.actorOf(Props[SearcherActor]
            .withDispatcher("echo.searcher.dispatcher"),
            name = "searcher"))

        crawler = context.actorOf(Props[CrawlerSupervisor], name = "crawler")
        context watch crawler

        directory = context.actorOf(Props[DirectoryBroker], name = "directory")
        context watch directory

        gateway = context.watch(context.actorOf(Props[GatewayActor]
            .withDispatcher("echo.gateway.dispatcher"),
            name = "gateway"))

        createCLI()
        /*
        cli = context.watch(context.actorOf(Props(new CliActor(self, index, parser, searcher, crawler, directory, gateway))
            .withDispatcher("echo.cli.dispatcher"),
            name = "cli"))
            */

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
        directory ! ActorRefDirectoryStoreActor(directory)

        log.info("up and running")
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case Terminated(corpse) => onTerminated(corpse)
        case ShutdownSystem()   => onSystemShutdown()
    }

    private def createCLI(): Unit = {
        cli = context.actorOf(Props(new CliActor(self, index, parser, searcher, crawler, directory, gateway))
            .withDispatcher("echo.cli.dispatcher"),
            name = "cli")
        context watch cli
    }

    private def onTerminated(corpse: ActorRef): Unit = {
        if (corpse == cli) {
            createCLI() // we simply re-create the CLI
        } else {
            log.error("Oh noh! A critical subsystem died : {}", corpse.path)
            self ! ShutdownSystem()
        }
    }

    private def onSystemShutdown(): Unit = {
        log.info("initiating shutdown sequence")

        // it is important to shutdown all actor(supervisor) befor shutting down the actor system
        context.system.stop(cli)
        context.system.stop(crawler)    // these have a too full inbox usually to let them finish processing
        context.system.stop(directory)
        context.system.stop(gateway)
        context.system.stop(index)
        context.system.stop(parser)
        context.system.stop(searcher)

        context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
        context.stop(self)  // master

    }

}
