package echo.actor

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import echo.actor.crawler.CrawlerActor
import echo.actor.indexer.IndexerActor
import .CrawlFeed
import .SearchQuery
import echo.actor.searcher.SearcherActor
import echo.actor.store.IndexStore

import scala.io.StdIn
;

object EchoApp extends App {

    var shutdown = false
    val usageMap = Map(
        "index" -> "feed [feed [feed]]",
        "search" -> "query [query [query]]"
    )

    println("Echo (actor-backend) started...")

    // create the system and actor
    val system = ActorSystem("EchoSystem")

    val indexRepo = system.actorOf(Props[IndexStore], name = "indexRepo")
    val indexer = system.actorOf(Props(classOf[IndexerActor], indexRepo), name = "indexer")
    val searcher = system.actorOf(Props(classOf[SearcherActor], indexRepo), name = "searcher")
    val crawler = system.actorOf(Props(classOf[CrawlerActor], indexer), name = "crawler")

    /*
    crawler ! CrawlFeed("someFeedUrl/Freakshow")
    crawler ! CrawlFeed("someFeedUrl/NotSafeForWork")
    crawler ! CrawlFeed("someFeedUrl/MethodischInkorrekt")

    // wait a while so the whole crawling/indexing/repo saving is done
    Thread.sleep(2000)

    searcher ! SearchQuery("Freakshow")

    // wait again to allow search process to finish
    Thread.sleep(1000)

    system.terminate()
    */

    repl()

    system.terminate()
    System.exit(0)

    def repl(): Unit = {

        while(!shutdown){
            val input = StdIn.readLine()
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true
                    case "index" :: Nil => usage("index")
                    case "index" :: feeds => feeds.foreach(f => crawler ! CrawlFeed(f))
                    case "search" :: Nil => usage("search")
                    case "search" :: query => searcher ! SearchQuery(query.mkString(" "))
                    case _  => usage("")
                }
            }
            exec(input.split(" "))
        }
    }

    def usage(cmd: String): Unit = {
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

    private def help() = {
        println("This is an interactive REPL providing a CLI to the search engine. Functions are:")
        println()
        for ( (k,v) <- usageMap ) {
            println(k + "\t" + v)
        }
        println()
        println("Feel free to play around!")
        println()
    }

}

