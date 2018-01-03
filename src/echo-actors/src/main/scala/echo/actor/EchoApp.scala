package irro.echo.actor

import akka.actor.{Actor, ActorSystem, Props}
import irro.echo.actor.crawler.{CrawlerActor, IndexRepo}
import irro.echo.actor.indexer.IndexerActor
import irro.echo.actor.protocol.CrawlerProtocol.CrawlFeed
import irro.echo.actor.protocol.SearchProtocol.SearchQuery
import irro.echo.actor.searcher.SearcherActor
;

object EchoApp extends App {

  println("Echo (actor-backend) started...")

  // create the system and actor
  val system = ActorSystem("EchoSystem")
  
  val indexRepo = system.actorOf(Props[IndexRepo], name = "indexRepo")
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
