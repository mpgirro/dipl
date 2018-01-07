package echo.actor.indexer

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.crawler.CrawlerActor
import echo.actor.protocol.IndexProtocol._

class IndexerActor (val indexRepo : ActorRef) extends Actor {

    val log = Logging(context.system, classOf[IndexerActor])

  override def receive: Receive = {

    case ProcessPodcastFeedData(feedData) => {
      log.info("Received ProcessPodcastFeedData('"+feedData+"') message")

      // TODO parse the feed data
      val podcast = feedData.split("/").last // TODO dummy to extract some data to index

      // TODO send messages to IndexRepo with the data to add/update to the index
      log.info("Sending AddPodcastToIndex('"+podcast+"') to IndexRepo")
      indexRepo ! AddPodcastToIndex(podcast)

      // TODO generate some random episode data to have to process them too
      log.info("Sending ProcessEpisodeFeedData(...) to self:Indexer")
      self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#1")
      self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#2")
      self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#3")
    }

    case ProcessEpisodeFeedData(feedRef, episode) => {
      log.info("Received ProcessEpisodeFeedData('"+feedRef+"','"+episode+"') message")

      // TODO this is a dummy processing of the episode data
      log.info("Sending AddEpisodeToIndex('"+feedRef+"','"+episode+"') to IndexRepo")
      indexRepo ! AddEpisodeToIndex(feedRef, episode)
    }

  }

}
