package echo.actor.cli

import java.util

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.collect.ImmutableList
import com.softwaremill.sttp._
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogProtocol._
import echo.core.benchmark._
import echo.core.benchmark.rtt.{ImmutableRoundTripTime, RoundTripTime}
import echo.core.util.{DocumentFormatter, UrlUtil}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, blocking}
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object CLI {
    final val name = "cli"
    def props(master: ActorRef,
              parser: ActorRef,
              searcher: ActorRef,
              crawler: ActorRef,
              catalogStore: ActorRef,
              gateway: ActorRef,
              updater: ActorRef,
              benchmarkMonitor: ActorRef): Props = {

        Props(new CLI(master, parser, searcher, crawler, catalogStore, gateway, updater, benchmarkMonitor))
            .withDispatcher("echo.cli.dispatcher")

    }
}

class CLI(master: ActorRef,
          parser: ActorRef,
          searcher: ActorRef,
          crawler: ActorRef,
          catalogStore: ActorRef,
          gateway: ActorRef,
          updater: ActorRef,
          benchmarkMonitor: ActorRef) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private val FEEDS_TXT = "../feeds.txt"
    private val MASSIVE_TXT = "../feeds_unique.txt"

    private val GATEWAY_URL = "http://localhost:3030/api"

    private var shutdown = false
    private val workQueue = new WorkQueue("Akka-Query-Queue",100)

    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "benchmark"      -> "<feed|index|search>",
        "benchmark feed" -> "feed <url>",
        "benchmark index"-> "",
        "benchmark search"-> "",
        "check podcast"  -> "[all|<exo>]",
        "check feed"     -> "[all|<exo>]",
        "count"          -> "[podcasts|episodes|feeds]",
        "search"         -> "query [query [query]]",
        "print database" -> "[podcasts|episodes|feeds]",
        "load feeds"     -> "[test|massive]",
        "load fyyd"      -> "[episodes <podcastId> <fyydId>]",
        "save feeds"     -> "<dest>",
        "crawl fyyd"     -> "count",
        "get podcast"    -> "<exo>",
        "get episode"    -> "<exo>",
        "request mean episodes" -> ""
    )

    private val feedPropertyUtil = new FeedPropertyUtil()

    private implicit val backend = HttpURLConnectionBackend()


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
            blocking {
                val input = StdIn.readLine()
                log.debug("CLI read : {}", input)

                Option(input).foreach(i => exec(i.split(" ")))
            }
        }

        log.info("Terminating due to user request")
        master ! ShutdownSystem
        workQueue.shutdown()
    }

    private def exec(commands: Array[String]): Unit = {
        commands.toList match {
            case "help" :: _ => help()
            case q@("q" | "quit" | "exit") :: _ => shutdown = true

            case "propose" :: Nil   => usage("propose")
            case "propose" :: feeds => feeds.foreach(f => updater ! ProposeNewFeed(f, RoundTripTime.empty()))

            case "benchmark" :: Nil                   => usage("benchmark")
            case "benchmark" :: "feed" :: Nil         => usage("benchmark feed")
            case "benchmark" :: "feed" :: feed :: Nil =>
                // TODO inform NodeMaster of benchmark
                val b = ImmutableRoundTripTime.builder()
                    .setId(feed)
                    .setLocation(feed)
                    .setWorkflow(Workflow.PODCAST_INDEX)
                    .create()
                updater ! ProposeNewFeed(feed, b)
            case "benchmark" :: "feed" :: feed :: _      => usage("benchmark feed")
            case "benchmark" :: "index" :: Nil           => benchmarkIndexSubsystem()
            case "benchmark" :: "index" :: _             => usage("benchmark index")
            case "benchmark" :: "search" :: Nil          => benchmarkRetrievalSubsystem(None)
            case "benchmark" :: "search" :: count :: Nil => benchmarkRetrievalSubsystem(Some(count))
            case "benchmark" :: "search" :: count :: _   => usage("benchmark search")

            case "check" :: "podcast" :: Nil           => usage("check podcast")
            case "check" :: "podcast" :: "all" :: Nil  => catalogStore ! CheckAllPodcasts
            case "check" :: "podcast" :: "all" :: _    => usage("check podcast")
            case "check" :: "podcast" :: exo :: Nil    => catalogStore ! CheckPodcast(exo)
            case "check" :: "podcast" :: _ :: _        => usage("check podcast")

            case "check" :: "feed" :: Nil              => usage("check feed")
            case "check" :: "feed" :: "all" :: Nil     => catalogStore ! CheckAllFeeds
            case "check" :: "feed" :: "all" :: _       => usage("check feed")
            case "check" :: "feed" :: exo :: Nil       => catalogStore ! CheckFeed(exo)
            case "check" :: "feed" :: _ :: _           => usage("check feed")

            case "count" :: "podcasts" :: Nil => catalogStore ! DebugPrintCountAllPodcasts
            case "count" :: "podcasts" :: _   => usage("count")
            case "count" :: "episodes" :: Nil => catalogStore ! DebugPrintCountAllEpisodes
            case "count" :: "episodes" :: _   => usage("count")
            case "count" :: "feeds" :: Nil    => catalogStore ! DebugPrintCountAllFeeds
            case "count" :: "feeds" :: _      => usage("count")
            case "count" :: _                 => usage("count")

            case "search" :: Nil    => usage("search")
            case "search" :: query  => search(query)

            case "print" :: "database" :: Nil               => usage("print database")
            case "print" :: "database" :: "podcasts" :: Nil => catalogStore ! DebugPrintAllPodcasts
            case "print" :: "database" :: "podcasts" :: _   => usage("print database")
            case "print" :: "database" :: "episodes" :: Nil => catalogStore ! DebugPrintAllEpisodes
            case "print" :: "database" :: "episodes" :: _   => usage("print database")
            case "print" :: "database" :: "feeds" :: Nil    => catalogStore ! DebugPrintAllFeeds
            case "print" :: "database" :: "feeds" :: _      => usage("print database")
            case "print" :: "database" :: _                 => usage("print database")
            case "print" :: _                               => help()

            case "load" :: Nil                         => help()
            case "load" :: "feeds" :: Nil              => usage("load feeds")
            case "load" :: "feeds" :: "test" :: Nil    => loadTestFeeds()
            case "load" :: "feeds" :: "massive" :: Nil => loadMassiveFeeds()
            case "load" :: "feeds" :: _                => usage("load feeds")

            case "load" :: "fyyd" :: "episodes" :: podcastId :: fyydId :: Nil => crawler ! LoadFyydEpisodes(podcastId, fyydId.toLong)
            case "load" :: "fyyd" :: _                                        => usage("load fyyd")

            case "save" :: Nil                    => help()
            case "save" :: "feeds" :: Nil         => usage("save feeds")
            case "save" :: "feeds" :: dest :: Nil => saveFeeds(dest)
            case "save" :: "feeds" :: _           => usage("save feeds")

            case "crawl" :: "fyyd" :: Nil          => usage("crawl fyyd")
            case "crawl" :: "fyyd" :: count :: Nil => crawler ! CrawlFyyd(count.toInt)
            case "crawl" :: "fyyd" :: count :: _   => usage("crawl fyyd")

            case "get" :: "podcast" :: Nil        => usage("get podcast")
            case "get" :: "podcast" :: exo :: Nil => getAndPrintPodcast(exo)
            case "get" :: "podcast" :: exo :: _   => usage("get podcast")

            case "get" :: "episode" :: Nil        => usage("get episode")
            case "get" :: "episode" :: exo :: Nil => getAndPrintEpisode(exo)
            case "get" :: "episode" :: exo :: _   => usage("get episode")

            case "request" :: "mean" :: "episodes" :: Nil => requestMeanEpisodeCountPerPodcast()
            case "request" :: "mean" :: "episodes" :: _   => usage("request mean episodes")

            case _  => help()
        }
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
        val future = searcher ? SearchRequest(query.mkString(" "), Some(1), Some(100), RoundTripTime.empty())
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[SearchResults]
        response match {
            case SearchResults(results, rtt) =>
                println("Found "+results.getResults.size()+" results for query '" + query.mkString(" ") + "'")
                println("Results:")
                for (result <- results.getResults.asScala) {
                    println(s"\n${DocumentFormatter.cliFormat(result)}\n")
                }
                println()
            case other => log.error("Received unexpected CatalogResult type : {}", other.getClass)
        }
    }

    private def getAndPrintPodcast(exo: String) = {
        val future = catalogStore ? GetPodcast(exo)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[CatalogQueryResult]
        response match {
            case PodcastResult(podcast)  => println(podcast.toString)
            case NothingFound(unknownId) => log.info("CatalogStore responded that there is no Podcast with EXO : {}", unknownId)
            case other                   => log.error("Received unexpected CatalogResult type : {}", other.getClass)
        }
    }

    private def getAndPrintEpisode(exo: String) = {
        val future = catalogStore ? GetEpisode(exo)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[CatalogQueryResult]
        response match {
            case EpisodeResult(episode)  => println(episode.toString)
            case NothingFound(unknownId) => log.info("CatalogStore responded that there is no Episode with EXO : {}", unknownId)
            case other                   => log.error("Received unexpected CatalogResult type : {}", other.getClass)
        }
    }

    private def loadTestFeeds(): Unit = {
        log.debug("Received LoadTestFeeds")
        for (feed <- Source.fromFile(FEEDS_TXT).getLines) {
            updater ! ProposeNewFeed(UrlUtil.sanitize(feed), RoundTripTime.empty())
        }
    }

    private def loadMassiveFeeds(): Unit = {
        log.debug("Received LoadMassiveFeeds")
        for (feed <- Source.fromFile(MASSIVE_TXT).getLines) {
            updater ! ProposeNewFeed(UrlUtil.sanitize(feed), RoundTripTime.empty())
        }
    }

    private def saveFeeds(dest: String): Unit = {
        log.debug("Received SaveFeeds : {}", dest)

        val future = catalogStore ? GetAllFeeds(0, 10000)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[CatalogQueryResult]
        response match {
            case AllFeedsResult(feeds)  =>
                import java.io._
                val pw = new PrintWriter(new File(dest))
                log.info("Writing {} feeds to file : {}", feeds.size, dest)
                feeds.foreach(f => pw.write(f.getUrl+ "\n"))
                pw.close()
            case other => log.error("Received unexpected CatalogResult type : {}", other.getClass)
        }
    }

    private def requestMeanEpisodeCountPerPodcast(): Unit = {
        log.debug("Requesting mean episode count per podcast from Catalog")
        val future = catalogStore ? GetMeanEpisodeCountPerPodcast
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[MeanEpisodeCountPerPodcast]
        log.info("Total Podcast Count   : {}", response.podcastCount)
        log.info("Total Episode Count   : {}", response.episodeCount)
        log.info("Mean Episodes/Podcast : {}", response.mean)
    }

    private def benchmarkIndexSubsystem(): Unit = {
        val feedProperties = feedPropertyUtil.loadProperties("../benchmark/properties.json") // TODO replace file path by something not hardcoded

        benchmarkMonitor ! MonitorFeedProgress(feedProperties)
        benchmarkMonitor ! StartMessagePerSecondMonitoring
        Thread.sleep(100) // ensure that the benchmark messages have been propagated to all actors (sent before guarantee only from CLI actor, not the NodeMaster!)

        for (fp <- feedProperties.asScala) {
            val location = "file://"+fp.getLocation
            val rtt = ImmutableRoundTripTime.builder()
                .setId(fp.getUri)
                .setLocation(location)
                .setWorkflow(Workflow.PODCAST_INDEX)
                .create()
            updater ! ProposeNewFeed(location, rtt) // TODO adjust location
        }
    }

    private def benchmarkRetrievalSubsystem(count: Option[String]): Unit = {
        val inputFile = count
            .map(c => "../benchmark/queries-lorem"+c+".txt")
            .getOrElse("../benchmark/queries.txt")
        val queries = loadBenchmarkQueries(inputFile)

        benchmarkMonitor ! MonitorQueryProgress(queries)
        benchmarkMonitor ! StartMessagePerSecondMonitoring
        Thread.sleep(100) // ensure that the benchmark messages have been propagated to all actors (sent before guarantee only from CLI actor, not the NodeMaster!)

        log.info(s"Sending ${queries.size()} search requests to ${GATEWAY_URL}")

        val rs = new util.LinkedList[Runnable]()
        for (q <- queries.asScala) {
            val r = new Runnable {
                override def run(): Unit = {
                    sttp.get(uri"${GATEWAY_URL}/benchmark-search?q=${q}&p=1&s=20")
                        //.body(rtt)
                        .send()
                }
            }
            rs.add(r)
        }

        workQueue.executeAll(rs)
    }

    private def loadBenchmarkQueries(filePath: String): ImmutableList[String] = {
        ImmutableList.copyOf(Source.fromFile(filePath).getLines.asJava)
    }

    private implicit val stringListSerializer: BodySerializer[ImmutableList[String]] = {
        qs: ImmutableList[String] =>
            if (qs == null || qs.isEmpty) {
                StringBody("[]", "UTF-8", Some("application/json"))
            } else {
                val serializedList = s"[${qs.asScala.map(q => "\""+q+"\"").mkString(", ")}]"
                println(serializedList)
                StringBody(serializedList, "UTF-8", Some("application/json"))
            }
    }

    private implicit val roundTripTimeSerializer: BodySerializer[RoundTripTime] = {
        rtt: RoundTripTime =>
            if (rtt == null) {
                StringBody("{}", "UTF-8", Some("application/json"))
            } else {
                val serializedTimestamps = if (rtt.getRtts == null || rtt.getRtts.isEmpty) "[]" else s"[${rtt.getRtts.asScala.mkString(", ")}]"
                val serializedRtt = s"""{\"id\":\"${rtt.getId}\", \"location\":\"${rtt.getLocation}\", \"workflow\":\"${rtt.getWorkflow.getName}\", \"rtts\":$serializedTimestamps}"""
                println(serializedRtt)
                StringBody(serializedRtt, "UTF-8", Some("application/json"))
            }
    }

}
