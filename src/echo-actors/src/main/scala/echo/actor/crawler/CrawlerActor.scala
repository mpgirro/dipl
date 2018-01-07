package echo.actor.crawler

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.protocol.Protocol._


class CrawlerActor (val indexer : ActorRef) extends Actor with ActorLogging {

    //val log = Logging(context.system, classOf[CrawlerActor])

    override def receive: Receive = {

        /*
        case CrawlFeed(feed) => {
            log.info("Received CrawlFeed('"+feed+"') message")
            val podcastName = feed.split("/").last // TODO generate some data and pretend it is the feed data

            log.info("Sending ProcessPodcastFeedData('"+podcastName+"') to Indexer")
            indexer ! ProcessPodcastFeedData(podcastName)
        }
        */

        case FetchNewFeed(feedUrl: String, podcastDocId: String) => {
            // TODO
        }

        case FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String]) => {
            // TODO
        }


    }
}
