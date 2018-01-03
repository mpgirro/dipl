package alokka.actor

import akka.actor.{Actor, ActorSystem, Props}
import alokka.actor.crawler.{CrawlerActor, IndexRepo}
import alokka.actor.indexer.IndexerActor
import alokka.actor.protocol.CrawlerProtocol.CrawlFeed
import alokka.actor.protocol.SearchProtocol.SearchQuery
import alokka.actor.searcher.SearcherActor
;

object AlokkaApp extends App {

  println("Alokka (actor-backend) started...")

  // create the system and actor
  val system = ActorSystem("AlokkaSystem")
  
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
