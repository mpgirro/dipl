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

    var shutdown = false
    val usageMap = Map(
        "propose" -> "feed [feed [feed]]",
        "search" -> "query [query [query]]"
    )

    println("Echo (actor-backend) started...")

    // create the system and actor
    val system = ActorSystem("EchoSystem")

    val indexStore = system.actorOf(Props[IndexStore], name = "indexStore")
    val indexer = system.actorOf(Props(classOf[IndexerActor], indexStore), name = "indexer")

    val searcher = system.actorOf(Props(classOf[SearcherActor], indexStore), name = "searcher")
    //val searcher = system.actorOf(Props(new SearcherActor(indexStore)), name = "searcher")

    val crawler = system.actorOf(Props(classOf[CrawlerActor], indexer), name = "crawler")
    val directoryStore = system.actorOf(Props(classOf[DirectoryStore], crawler), name = "directoryStore")

    repl()

    system.terminate()
    System.exit(0)

    private def repl() {

        while(!shutdown){
            val input = StdIn.readLine()
            def exec(commands: Array[String]): Unit = {
                commands.toList match {
                    case "help" :: _ => help()
                    case q@("q" | "quit" | "exit") :: _ => shutdown = true
                    case "propose" :: Nil => usage("propose")
                    case "propose" :: feeds => feeds.foreach(f => directoryStore ! ProposeNewFeed(f))
                    case "search" :: Nil => usage("search")
                    case "search" :: query => {
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
                    case "print-database" :: _ => directoryStore ! DebugPrintAllDatabase
                    case _  => usage("")
                }
            }
            exec(input.split(" "))
        }
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

}

