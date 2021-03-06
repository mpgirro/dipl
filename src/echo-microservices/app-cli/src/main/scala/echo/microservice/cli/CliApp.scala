package echo.microservice.cli

import java.util

import com.google.common.collect.ImmutableList
import com.typesafe.scalalogging.Logger
import com.softwaremill.sttp._
import echo.core.async.catalog.{ImmutableProposeNewFeedJob, ProposeNewFeedJob}
import echo.core.benchmark._
import echo.core.benchmark.rtt.{ImmutableRoundTripTime, RoundTripTime}
import echo.core.util.UrlUtil

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.io.{Source, StdIn}

object CliApp {
    def main(args:Array[String]){
        val app = new CliApp
        app.repl()
    }
}

class CliApp {

    private val log = Logger(classOf[CliApp])

    private var shutdown = false
    private val workQueue = new WorkQueue("MSA-Query-Queue", 100)

    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
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
        "benchmark"      -> "<index|search>",
        "benchmark index"-> "",
        "benchmark search"-> ""
    )

    private val feedPropertyUtil = new FeedPropertyUtil()

    // TODO
    private val GATEWAY_URL = "http://localhost:3030"
    private val CATALOG_URL = "http://localhost:3031/catalog"
    private val REGISTRY_URL = "http://localhost:3036"
    private val UPDATER_URL = "http://localhost:3037"
    private val FEEDS_TXT = "../../feeds.txt"

    private implicit val backend = HttpURLConnectionBackend()

    private def repl() {
        log.info("CLI read to take commands")
        while(!shutdown){
            val input = StdIn.readLine()
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true

                    case "propose" :: Nil   => usage("propose")
                    case "propose" :: feeds => feeds.foreach(f => propose(f))

                    case "load" :: Nil                          => help()
                    case "load" :: "feeds" :: Nil               => usage("load feeds")
                    case "load" :: "feeds" :: "test" :: Nil     => loadTestFeeds()
                    case "load" :: "feeds" :: _                 => usage("load feeds")

                    case "benchmark" :: "index" :: Nil  => benchmarkIndexSubsystem()
                    case "benchmark" :: "index" :: _    => usage("benchmark index")
                    case "benchmark" :: "search" :: Nil          => benchmarkRetrievalSubsystem(None)
                    case "benchmark" :: "search" :: count :: Nil => benchmarkRetrievalSubsystem(Some(count))
                    case "benchmark" :: "search" :: count :: _   => usage("benchmark search")
                    case "benchmark" :: _               => usage("benchmark")

                    case _  => help()
                }
            }
            Option(input).foreach(i => exec(i.split(" ")))
        }

        log.info("Terminating due to user request")
        workQueue.shutdown()
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

    private def propose(url: String): Unit = {
        // TODO
        sttp.post(uri"${CATALOG_URL}/feed/propose?url=${url}").send()
    }

    private implicit val stringListSerializer: BodySerializer[ImmutableList[String]] = {
        qs: ImmutableList[String] =>
            val serializedList = s"[${qs.asScala.map(q => "\""+q+"\"").mkString(", ")}]"
            println(serializedList)
            StringBody(serializedList, "UTF-8", Some("application/json"))
    }

    private implicit val feedPropertiesSerializer: BodySerializer[ImmutableList[FeedProperty]] = {
        ps: ImmutableList[FeedProperty] =>
            val serializedProperties = ps.asScala
                .map(p => s"""{\"uri\":\"${p.getUri}\", \"location\":\"${p.getLocation}\", \"numberOfEpisodes\":${p.getNumberOfEpisodes} }""")
                .mkString(",")
            val serializedList = s"[$serializedProperties]"
            println(serializedList)
            StringBody(serializedList, "UTF-8", Some("application/json"))
    }

    private implicit val roundTripTimeSerializer: BodySerializer[RoundTripTime] = {
        rtt: RoundTripTime =>
            val serializedTimestamps = s"[${rtt.getRtts.asScala.mkString(", ")}]"
            val serializedRtt = s"""{\"id\":\"${rtt.getId}\", \"location\":\"${rtt.getLocation}\", \"workflow\":\"${rtt.getWorkflow.getName}\", \"rtts\":$serializedTimestamps}"""
            println(serializedRtt)
            StringBody(serializedRtt, "UTF-8", Some("application/json"))
    }

    private def serialize(job: ProposeNewFeedJob): String = {
        val serializedTimestamps = s"[${job.getRtt.getRtts.asScala.mkString(", ")}]"
        val serializedRtt = s"""{\"id\":\"${job.getRtt.getId}\", \"location\":\"${job.getRtt.getLocation}\", \"workflow\":\"${job.getRtt.getWorkflow.getName}\", \"rtts\":$serializedTimestamps}"""

        s"""{\"feed\":\"${job.getFeed}\", \"rtt\":$serializedRtt}"""
    }

    private implicit val proposeNewFeedJobSerializer: BodySerializer[ProposeNewFeedJob] = {
        job: ProposeNewFeedJob =>
            val serializedJob = serialize(job)
            StringBody(serializedJob, "UTF-8", Some("application/json"))
    }

    private implicit val proposeNewFeedJobsSerializer: BodySerializer[List[ProposeNewFeedJob]] = {
        jobs: List[ProposeNewFeedJob] =>
            val serializedJobs = jobs
                .map(j => serialize(j))
                .mkString(",")
            val serializedList = s"[$serializedJobs]"
            StringBody(serializedList, "UTF-8", Some("application/json"))
    }

    private def loadTestFeeds(): Unit = {
        log.debug("Received LoadTestFeeds")
        for (feed <- Source.fromFile(FEEDS_TXT).getLines) {
            log.info("Proposing feed : {}", feed)
            propose(UrlUtil.sanitize(feed))
        }
    }

    private def benchmarkIndexSubsystem(): Unit = {
        val feedProperties = feedPropertyUtil.loadProperties("../../benchmark/properties.json") // TODO replace file path by something not hardcoded

        log.info("Transmitting feed data for progress monitoring")
        sttp.post(uri"${REGISTRY_URL}/benchmark/monitor-feed-progress")
            .body(feedProperties)
            .send()
        startMessagePerSecondMonitoring()

        val jobs = new mutable.ListBuffer[ProposeNewFeedJob]
        for (fp <- feedProperties.asScala) {
            val location = "file://"+fp.getLocation
            val rtt = ImmutableRoundTripTime.builder()
                .setId(fp.getUri)
                .setLocation(location)
                .setWorkflow(Workflow.PODCAST_INDEX)
                .create()

            val job = ImmutableProposeNewFeedJob.of(location, rtt)

            /*
            sttp.post(uri"${UPDATER_URL}/updater/propose-new-feed")
                .body(job)
                .send()
            */

            jobs += job
        }

        log.info("Transmitting list of feeds to propose")
        sttp.post(uri"${UPDATER_URL}/updater/propose-feeds")
            .body(jobs.toList)
            .send()
    }

    private def benchmarkRetrievalSubsystem(count: Option[String]): Unit = {
        val inputFile = count
            .map(c => "../../benchmark/queries-lorem"+c+".txt")
            .getOrElse("../../benchmark/queries.txt")
        val queries = loadBenchmarkQueries(inputFile)

        log.info("Transmitting query data for progress monitoring")
        sttp.post(uri"${REGISTRY_URL}/benchmark/monitor-query-progress")
            .body(queries)
            .send()
        startMessagePerSecondMonitoring()

        log.info(s"Sending ${queries.size()} search requests to ${GATEWAY_URL}")

        val rs = new util.LinkedList[Runnable]()
        for (q <- queries.asScala) {
            val rtt = ImmutableRoundTripTime.builder()
                .setId(q)
                .setLocation("")
                .setWorkflow(Workflow.RESULT_RETRIEVAL)
                .create()
            val r = new Runnable {
                override def run(): Unit = {
                    sttp.post(uri"${GATEWAY_URL}/benchmark/search?q=${q}&p=1&s=20")
                        .body(rtt)
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

    private def startMessagePerSecondMonitoring(): Unit = {
        log.info("Sending msg to start MPS meters")
        sttp.post(uri"${REGISTRY_URL}/benchmark/start-mps")
            .send()
    }

}




