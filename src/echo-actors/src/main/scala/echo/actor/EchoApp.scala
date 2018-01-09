package echo.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import akka.pattern.ask
import echo.actor.crawler.CrawlerActor
import echo.actor.indexer.IndexerActor
import echo.actor.protocol.Protocol._
import echo.actor.searcher.SearcherActor
import echo.actor.store.{DirectoryStore, IndexStore}
import echo.core.dto.document.{Document, EpisodeDocument, PodcastDocument}
import echo.core.util.DocumentFormatter

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.io.StdIn
;

object EchoApp extends App {

    private var shutdown = false
    val usageMap = Map(
        "propose" -> "feed [feed [feed]]",
        "search" -> "query [query [query]]",
        "print database" -> "",
        "test-index" -> "",
        "crawl fyyd" -> "count"
    )

    // create the system and actor
    val system = ActorSystem("EchoSystem")

    val indexStore = system.actorOf(Props[IndexStore], name = "indexStore")
    val indexer = system.actorOf(Props(classOf[IndexerActor], indexStore), name = "indexer")
    val searcher = system.actorOf(Props(classOf[SearcherActor], indexStore), name = "searcher")
    val crawler = system.actorOf(Props(classOf[CrawlerActor], indexer), name = "crawler")
    val directoryStore = system.actorOf(Props(classOf[DirectoryStore], crawler), name = "directoryStore")

    // pass around references not provided by constructors due to circular dependencies
    crawler ! ActorRefDirectoryStoreActor(directoryStore)
    indexer ! ActorRefDirectoryStoreActor(directoryStore)


    repl()

    system.terminate()
    System.exit(0)

    private def repl() {

        println("> Welcome to Echo:Actor Engine interactive exploration App!")

        while(!shutdown){
            print("> ")
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
                    case "print" :: _ => help()

                    case "test" :: "index" :: _ => testIndex()
                    case "test" :: _            => help()

                    case "crawl" :: "fyyd" :: Nil           => usage("crawl-fyyd")
                    case "crawl" :: "fyyd" :: count :: Nil  => crawler ! CrawlFyyd(count.toInt)
                    case "crawl" :: "fyyd" :: count :: _    => usage("crawl-fyyd")

                    case _  => help()
                }
            }
            exec(input.split(" "))
        }

        println("Bye!\n")
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
        implicit val timeout = Timeout(5 seconds)
        val future = searcher ? SearchRequest(query.mkString(" "))
        val response = Await.result(future, timeout.duration).asInstanceOf[SearchResults]
        response match {

            case SearchResults(results) => {
                println("Found "+results.length+" results for query '" + query.mkString(" ") + "'");
                println("Results:")
                for (doc <- results) {
                    println()
                    println(new DocumentFormatter().format(doc))
                    println()
                }
                println()
            }
        }
    }

    private def testIndex(): Unit ={
        directoryStore ! ProposeNewFeed("https://feeds.metaebene.me/freakshow/m4a")
        directoryStore ! ProposeNewFeed("http://www.fanboys.fm/episodes.mp3.rss")
        directoryStore ! ProposeNewFeed("http://falter-radio.libsyn.com/rss")
        directoryStore ! ProposeNewFeed("http://revolutionspodcast.libsyn.com/rss/")
        directoryStore ! ProposeNewFeed("https://feeds.metaebene.me/forschergeist/m4a")
        directoryStore ! ProposeNewFeed("http://feeds.soundcloud.com/users/soundcloud:users:325487962/sounds.rss")
    }

}

