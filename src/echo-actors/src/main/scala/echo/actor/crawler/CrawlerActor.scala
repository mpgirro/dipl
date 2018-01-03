package alokka.actor.crawler

import akka.actor.{Actor, ActorLogging, ActorRef}
import alokka.actor.protocol.CrawlerProtocol.CrawlFeed
import alokka.actor.protocol.IndexProtocol.{AddPodcastToIndex, ProcessPodcastFeedData}


class CrawlerActor (val indexer : ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {

    case CrawlFeed(feed) => {
      log.info("Received CrawlFeed('"+feed+"') message")
      val podcastName = feed.split("/").last // TODO generate some data and pretend it is the feed data

      log.info("Sending ProcessPodcastFeedData('"+podcastName+"') to Indexer")
      indexer ! ProcessPodcastFeedData(podcastName)
    }


  }
}
