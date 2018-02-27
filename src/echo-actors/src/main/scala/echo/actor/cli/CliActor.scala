package echo.actor.cli

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.util.{DocumentFormatter, UrlUtil}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class CliActor(private val master: ActorRef,
               private val indexStore: ActorRef,
               private val parser: ActorRef,
               private val searcher: ActorRef,
               private val crawler: ActorRef,
               private val directoryStore: ActorRef,
               private val gateway: ActorRef) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private var shutdown = false

    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "check podcast"  -> "[all|<echoId>]",
        "check feed"     -> "[all|<echoId>]",
        "count"          -> "[podcasts|episodes|feeds]",
        "search"         -> "query [query [query]]",
        "print database" -> "[podcasts|episodes|feeds]",
        "load feeds"     -> "[test|massive]",
        "save feeds"     -> "<dest>",
        "crawl fyyd"     -> "count",
        "get podcast"    -> "<echoId>",
        "get episode"    -> "<echoId>"
    )


    // to the REPL, if it terminates, then a poison pill is sent to self and the system will subsequently shutdown too
    repl()

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case unhandled => log.info("Received " + unhandled)
    }

    private def repl() {

        log.info("CLI read to take commands")

        while(!shutdown){
            val input = StdIn.readLine()
            log.debug("CLI read : {}", input)
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true

                    case "propose" :: Nil   => usage("propose")
                    case "propose" :: feeds => feeds.foreach(f => directoryStore ! ProposeNewFeed(f))

                    case "check" :: "podcast" :: Nil           => usage("check podcast")
                    case "check" :: "podcast" :: "all" :: Nil  => directoryStore ! CheckAllPodcasts
                    case "check" :: "podcast" :: "all" :: _    => usage("check podcast")
                    case "check" :: "podcast" :: echoId :: Nil => directoryStore ! CheckPodcast(echoId)
                    case "check" :: "podcast" :: _ :: _        => usage("check podcast")

                    case "check" :: "feed" :: Nil              => usage("check feed")
                    case "check" :: "feed" :: "all" :: Nil     => directoryStore ! CheckAllFeeds
                    case "check" :: "feed" :: "all" :: _       => usage("check feed")
                    case "check" :: "feed" :: echoId :: Nil    => directoryStore ! CheckFeed(echoId)
                    case "check" :: "feed" :: _ :: _           => usage("check feed")

                    case "count" :: "podcasts" :: Nil => directoryStore ! DebugPrintCountAllPodcasts
                    case "count" :: "podcasts" :: _   => usage("count")
                    case "count" :: "episodes" :: Nil => directoryStore ! DebugPrintCountAllEpisodes
                    case "count" :: "episodes" :: _   => usage("count")
                    case "count" :: "feeds" :: Nil    => directoryStore ! DebugPrintCountAllFeeds
                    case "count" :: "feeds" :: _      => usage("count")
                    case "count" :: _                 => usage("count")

                    case "search" :: Nil    => usage("search")
                    case "search" :: query  => search(query)

                    case "print" :: "database" :: Nil               => usage("print database")
                    case "print" :: "database" :: "podcasts" :: Nil => directoryStore ! DebugPrintAllPodcasts
                    case "print" :: "database" :: "podcasts" :: _   => usage("print database")
                    case "print" :: "database" :: "episodes" :: Nil => directoryStore ! DebugPrintAllEpisodes
                    case "print" :: "database" :: "episodes" :: _   => usage("print database")
                    case "print" :: "database" :: "feeds" :: Nil    => directoryStore ! DebugPrintAllFeeds
                    case "print" :: "database" :: "feeds" :: _      => usage("print database")
                    case "print" :: "database" :: _                 => usage("print database")
                    case "print" :: _                               => help()

                    case "load" :: Nil                          => help()
                    case "load" :: "feeds" :: Nil               => usage("load feeds")
                    case "load" :: "feeds" :: "test" :: Nil     => loadTestFeeds()
                    case "load" :: "feeds" :: "massive" :: Nil  => loadMassiveFeeds()
                    case "load" :: "feeds" :: _                 => usage("load feeds")

                    case "save" :: Nil                          => help()
                    case "save" :: "feeds" :: Nil               => usage("save feeds")
                    case "save" :: "feeds" :: dest :: Nil       => saveFeeds(dest)
                    case "save" :: "feeds" :: _                 => usage("save feeds")

                    case "crawl" :: "fyyd" :: Nil           => usage("crawl fyyd")
                    case "crawl" :: "fyyd" :: count :: Nil  => crawler ! CrawlFyyd(count.toInt)
                    case "crawl" :: "fyyd" :: count :: _    => usage("crawl fyyd")

                    case "get" :: "podcast" :: Nil           => usage("get podcast")
                    case "get" :: "podcast" :: echoId :: Nil => getAndPrintPodcast(echoId)
                    case "get" :: "podcast" :: echoId :: _   => usage("get podcast")

                    case "get" :: "episode" :: Nil           => usage("get episode")
                    case "get" :: "episode" :: echoId :: Nil => getAndPrintEpisode(echoId)
                    case "get" :: "episode" :: echoId :: _   => usage("get episode")

                    case _  => help()
                }
            }
            exec(input.split(" "))
        }

        log.info("Terminating due to user request")
        master ! ShutdownSystem()
    }

    private def usage(cmd: String): Unit = {
        if (usageMap.contains(cmd)) {
            val args = usageMap.get(cmd)
            println("Command parsing error")
            println("Usage: " + cmd + " " + args)
        } else {
            println("Unknown command: " + cmd)
            println("These are the available commands:")
            for ( (k,v) <- usageMap ) {
                println(k + "\t" + v)
            }
        }
    }

    private def help(): Unit = {
        println("This is an interactive REPL providing a CLI to the search engine. Functions are:\n")
        for ( (k,v) <- usageMap ) {
            println(k + "\t" + v)
        }
        println("\nFeel free to play around!\n")
    }

    private def search(query: List[String]): Unit = {
        val future = searcher ? SearchRequest(query.mkString(" "), Some(1), Some(100))
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[SearchResults]
        response match {
            case SearchResults(results) =>
                println("Found "+results.getResults.length+" results for query '" + query.mkString(" ") + "'")
                println("Results:")
                for (result <- results.getResults) {
                    println(s"\n${DocumentFormatter.cliFormat(result)}\n")
                }
                println()
            case other => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def getAndPrintPodcast(echoId: String) = {
        val future = directoryStore ? GetPodcast(echoId)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryResult]
        response match {
            case PodcastResult(podcast)  => println(DocumentFormatter.cliFormat(podcast))
            case NothingFound(unknownId) => log.info("DirectoryStore responded that there is no Podcast with echoId : {}", unknownId)
            case other                   => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def getAndPrintEpisode(echoId: String) = {
        val future = directoryStore ? GetEpisode(echoId)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryResult]
        response match {
            case EpisodeResult(episode)  => println(DocumentFormatter.cliFormat(episode))
            case NothingFound(unknownId) => log.info("DirectoryStore responded that there is no Episode with echoId : {}", unknownId)
            case other                   => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def loadTestFeeds(): Unit = {
        log.debug("Received LoadTestFeeds")

        val filename = "../feeds.txt"
        for (feed <- Source.fromFile(filename).getLines) {
            directoryStore ! ProposeNewFeed(UrlUtil.sanitize(feed))
        }
        log.debug("Finished LoadTestFeeds")
    }

    private def loadMassiveFeeds(): Unit = {
        log.debug("Received LoadMassiveFeeds")

        val filename = "../feeds_unique.txt"
        for (feed <- Source.fromFile(filename).getLines) {
            directoryStore ! ProposeNewFeed(UrlUtil.sanitize(feed))
        }
        log.debug("Finished LoadMassiveFeeds")
    }

    private def saveFeeds(dest: String): Unit = {
        log.debug("Received SaveFeeds : {}", dest)

        val future = directoryStore ? GetAllFeeds(0, 10000)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryResult]
        response match {
            case AllFeedsResult(feeds)  =>
                import java.io._
                val pw = new PrintWriter(new File(dest))
                log.info("Writing {} feeds to file : {}", feeds.size, dest)
                feeds.foreach(f => pw.write(f.getUrl+ "\n"))
                pw.close()
            case other => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
        log.debug("Finished SaveFeeds : {}", dest)
    }

}
