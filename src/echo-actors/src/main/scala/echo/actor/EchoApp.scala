package echo.actor

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import echo.actor.crawler.CrawlerActor
import echo.actor.indexer.IndexerActor
import echo.actor.protocol.CrawlerProtocol.CrawlFeed
import echo.actor.protocol.SearchProtocol.SearchQuery
import echo.actor.searcher.SearcherActor
import echo.actor.store.IndexStore
;

object EchoApp extends App {

  println("Echo (actor-backend) started...")

  // create the system and actor
  val system = ActorSystem("EchoSystem")

  val indexRepo = system.actorOf(Props[IndexStore], name = "indexRepo")
  val indexer = system.actorOf(Props(classOf[IndexerActor], indexRepo), name = "indexer")
  val searcher = system.actorOf(Props(classOf[SearcherActor], indexRepo), name = "searcher")
  val crawler = system.actorOf(Props(classOf[CrawlerActor], indexer), name = "crawler")

  crawler ! CrawlFeed("someFeedUrl/Freakshow")
  crawler ! CrawlFeed("someFeedUrl/NotSafeForWork")
  crawler ! CrawlFeed("someFeedUrl/MethodischInkorrekt")

  // wait a while so the whole crawling/indexing/repo saving is done
  Thread.sleep(2000)

  searcher ! SearchQuery("Freakshow")

  // wait again to allow search process to finish
  Thread.sleep(1000)

  system.terminate()
}
