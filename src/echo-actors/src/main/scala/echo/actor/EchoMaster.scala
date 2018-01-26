package echo.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.pattern.ask
import akka.util.Timeout
import echo.actor.crawler.CrawlerActor
import echo.actor.gateway.GatewayActor
import echo.actor.indexer.IndexerActor
import echo.actor.protocol.ActorMessages._
import echo.actor.searcher.SearcherActor
import echo.actor.store.{DirectoryStore, IndexStore}
import echo.core.util.DocumentFormatter

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class EchoMaster extends Actor with ActorLogging {

    override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

    private var shutdown = false
    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "search"         -> "query [query [query]]",
        "print database" -> "",
        "test index"     -> "",
        "crawl fyyd"     -> "count",
        "get podcast"    -> "<echoId>",
        "get episode"    -> "<echoId>"
    )

    private val indexStore = context.watch(context.actorOf(Props[IndexStore].withDispatcher("echo.index-store.dispatcher"), "indexStore"))
    private val indexer = context.watch(context.actorOf(Props[IndexerActor], name = "indexer"))
    private val searcher = context.watch(context.actorOf(Props(classOf[SearcherActor], indexStore), name = "searcher"))
    private val crawler = context.watch(context.actorOf(Props[CrawlerActor].withDispatcher("echo.crawler.dispatcher"), name = "crawler"))
    private val directoryStore = context.watch(context.actorOf(Props(classOf[DirectoryStore], crawler), name = "directoryStore"))
    private val gateway = context.watch(context.actorOf(Props[GatewayActor], name = "gateway"))

    // pass around references not provided by constructors due to circular dependencies
    crawler ! ActorRefIndexerActor(indexer)
    crawler ! ActorRefDirectoryStoreActor(directoryStore)

    indexer ! ActorRefIndexStoreActor(indexStore)
    indexer ! ActorRefDirectoryStoreActor(directoryStore)
    indexer ! ActorRefCrawlerActor(crawler)

    gateway ! ActorRefSearcherActor(searcher)
    gateway ! ActorRefDirectoryStoreActor(directoryStore)

    directoryStore ! ActorRefIndexStoreActor(indexStore)


    log.info("EchoMaster up and running")

    // to the REPL, if it terminates, then a poison pill is sent to self and the system will subsequently shutdown too
    repl()

    override def receive = {
        case Terminated(actor) => onTerminated(actor)
    }

    protected def onTerminated(actor: ActorRef): Unit = {
        log.error("Terminating the system because {} terminated!", actor)
        context.system.terminate()
    }

    private def repl() {

        println("> Welcome to Echo:Actor Engine interactive exploration App!")

        while(!shutdown){
            val input = StdIn.readLine()
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true

                    case "propose" :: Nil   => usage("propose")
                    case "propose" :: feeds => feeds.foreach(f => directoryStore ! ProposeNewFeed(f))

                    case "search" :: Nil    => usage("search")
                    case "search" :: query  => search(query)

                    case "print" :: "database" :: Nil   => directoryStore ! DebugPrintAllDatabase
                    case "print" :: "database" :: _     => usage("print database")
                    case "print" :: _                   => help()

                    case "test" :: "index" :: _ => testIndex()
                    case "test" :: _            => help()

                    case "crawl" :: "fyyd" :: Nil           => usage("crawl fyyd")
                    case "crawl" :: "fyyd" :: count :: Nil  => crawler ! CrawlFyyd(count.toInt)
                    case "crawl" :: "fyyd" :: count :: _    => usage("crawl fyyd")

                    case "get" :: "podcast" :: Nil           => usage("get podcast")
                    case "get" :: "podcast" :: echoId :: Nil => getPodcast(echoId)
                    case "get" :: "podcast" :: echoId :: _   => usage("get podcast")

                    case "get" :: "episode" :: Nil           => usage("get episode")
                    case "get" :: "episode" :: echoId :: Nil => getEpisode(echoId)
                    case "get" :: "episode" :: echoId :: _   => usage("get episode")

                    case _  => help()
                }
            }
            exec(input.split(" "))
        }

        log.info("Terminating the system due to CLI request")
        context.system.terminate()
    }

    private def usage(cmd: String) {
        if (usageMap.contains(cmd)) {
            val args = usageMap.get(cmd)
            println("Command parsing error")
            println("Usage: " + cmd + " " + args)
        }
        else {
            println("Unknown command: " + cmd)
            println("These are the available commands:")
            for ( (k,v) <- usageMap ) {
                println(k + "\t" + v)
            }
        }
    }

    private def help() {
        println("This is an interactive REPL providing a CLI to the search engine. Functions are:")
        println()
        for ( (k,v) <- usageMap ) {
            println(k + "\t" + v)
        }
        println()
        println("Feel free to play around!")
        println()
    }

    private def search(query: List[String]): Unit = {
        implicit val timeout = Timeout(10 seconds)
        val future = searcher ? SearchRequest(query.mkString(" "), 1, 100)
        val response = Await.result(future, timeout.duration).asInstanceOf[SearchResults]
        response match {

            case SearchResults(results) => {
                println("Found "+results.getResults.length+" results for query '" + query.mkString(" ") + "'");
                println("Results:")
                for (result <- results.getResults) {
                    println()
                    println(DocumentFormatter.cliFormat(result))
                    println()
                }
                println()
            }
        }
    }

    private def testIndex(): Unit ={
        val filename = "../feeds.txt"
        for (feed <- Source.fromFile(filename).getLines) {
            directoryStore ! ProposeNewFeed(feed)
        }
    }

    private def getPodcast(echoId: String) = {
        implicit val timeout = Timeout(5 seconds)
        val future = directoryStore ? GetPodcast(echoId)
        val response = Await.result(future, timeout.duration).asInstanceOf[DirectoryResult]
        response match {
            case PodcastResult(podcast) => {
                println(DocumentFormatter.cliFormat(podcast))
            }

            case NoDocumentFound(unknownId: String) => {
                println("DirectoryStore responded that there is no Podcast with echoId={}", unknownId)
            }
        }
    }

    private def getEpisode(echoId: String) = {
        implicit val timeout = Timeout(5 seconds)
        val future = directoryStore ? GetEpisode(echoId)
        val response = Await.result(future, timeout.duration).asInstanceOf[DirectoryResult]
        response match {
            case EpisodeResult(episode) => {
                println(DocumentFormatter.cliFormat(episode))
            }

            case NoDocumentFound(unknownId: String) => {
                println("DirectoryStore responded that there is no Episode with echoId={}", unknownId)
            }
        }
    }
}
