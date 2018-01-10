package echo.actor.crawler

import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.util.Scanner

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.protocol.ActorMessages._
import echo.core.feed.FeedStatus
import echo.core.parse.api.FyydAPI

class CrawlerActor (val indexer: ActorRef) extends Actor with ActorLogging {

    //val log = Logging(context.system, classOf[CrawlerActor])

    private var directoryStore: ActorRef = _

    val fyydAPI: FyydAPI = new FyydAPI();

    override def receive: Receive = {

        case ActorRefDirectoryStoreActor(ref) => {
            log.debug("Received ActorRefDirectoryStoreActor")
            directoryStore = ref;
        }

        case FetchNewFeed(feedUrl: String, podcastDocId: String) => {

            log.info("Received FetchNewFeed for feed: {}", feedUrl)
            try {
                val feedData = download(feedUrl)

                // send downloaded data to Indexer for processing
                indexer ! IndexFeedData(feedUrl, podcastDocId, Array.empty, feedData)

                // send status to DirectoryStore
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } catch {
                case e: IOException => {
                    log.error("Could not download feed from: {}", feedUrl)
                    directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            }
        }

        case FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String]) => {

            log.info("Received FetchUpdateFeed for feed: {}", feedUrl)
            val feedData = download(feedUrl)
            if(feedData != null){
                // send downloaded data to Indexer for processing
                indexer ! IndexFeedData(feedUrl, podcastDocId, episodeDocIds, feedData)

                // send status to DirectoryStore
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } else {
                log.error("Could not download feed from: {}", feedUrl)
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }
        }

        case CrawlFyyd(count) => {
            log.debug("Received CrawlFyyd({})", count)
            val feeds = fyydAPI.getFeedUrls(count);
            log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)

            log.debug("Proposing these feeds to the internal directory now")
            val it = feeds.iterator()
            while(it.hasNext){
                directoryStore ! ProposeNewFeed(it.next())
            }
        }

    }

    // TODO this method must be updated to do all the pro crawling stuff (best using a decent HTTP client lib)
    private def download(feedUrl: String): String = {
        try {
            val scanner = new Scanner(new URL(feedUrl).openStream, "UTF-8").useDelimiter("\\A")
            if(scanner.hasNext){
                return scanner.next
            }
        } catch {
            case e: IOException => {
                log.error("Exception while loading feed: {} ; reason: {}", feedUrl, e.getMessage)
                return null;
            }
        }
        return null;
    }



}
