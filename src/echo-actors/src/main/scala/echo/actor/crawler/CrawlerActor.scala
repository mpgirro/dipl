package echo.actor.crawler

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.protocol.Protocol._
import echo.core.feed.FeedStatus

import scala.collection.mutable.ArrayBuffer


class CrawlerActor (val indexer: ActorRef) extends Actor with ActorLogging {

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

            log.info("Received FetchNewFeed for feed: " + feedUrl)

            // TODO
            val feedData = download(feedUrl)
            indexer ! IndexFeedData(feedUrl, podcastDocId, Array.empty, feedData)

            // reply to the DirectoryStore
            sender ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.SUCCESS)
        }

        case FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String]) => {

            log.info("Received FetchUpdateFeed for feed: " + feedUrl)

            // TODO
            val feedData = download(feedUrl)
            indexer ! IndexFeedData(feedUrl, podcastDocId, episodeDocIds, feedData)

            // reply to the DirectoryStore
            sender ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.SUCCESS)
        }


    }

    // TODO this method must be updated to do all the pro crawling stuff (best using a decent HTTP client lib)
    private def download(feedUrl: String): String = {
        val url = new URL(feedUrl)
        val in = new BufferedReader(new InputStreamReader(url.openStream))
        val buffer = new ArrayBuffer[String]()
        var inputLine = in.readLine
        while (inputLine != null) {
            if (!inputLine.trim.equals("")) {
                buffer += inputLine.trim
            }
            inputLine = in.readLine
        }
        in.close

        return buffer.toList.mkString("")
    }



}
