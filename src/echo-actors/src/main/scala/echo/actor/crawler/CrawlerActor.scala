package echo.actor.crawler

import java.io.IOException
import java.net.URL
import java.time.LocalDateTime
import java.util.Scanner

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.protocol.Protocol._
import echo.core.feed.FeedStatus

class CrawlerActor (val indexer: ActorRef) extends Actor with ActorLogging {

    //val log = Logging(context.system, classOf[CrawlerActor])

    override def receive: Receive = {

        case FetchNewFeed(feedUrl: String, podcastDocId: String) => {

            log.debug("Received FetchNewFeed for feed: " + feedUrl)
            try {
                val feedData = download(feedUrl)

                indexer ! IndexFeedData(feedUrl, podcastDocId, Array.empty, feedData)

                // reply to the DirectoryStore
                // TODO better to directly address directoryStore once I have the actorRef
                sender ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } catch {
                case e: IOException => {
                    log.error("Could not download feed from: feedUrl")
                    // TODO send FeedStatusUpdate once we have the actorRef (cyclic dependency and such...)
                    // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            }
        }

        case FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String]) => {

            log.debug("Received FetchUpdateFeed for feed: " + feedUrl)
            try {
                val feedData = download(feedUrl)
                indexer ! IndexFeedData(feedUrl, podcastDocId, episodeDocIds, feedData)

                // reply to the DirectoryStore
                // TODO better to directly address directoryStore once I have the actorRef
                sender ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } catch {
                case e: IOException => {
                    log.error("Could not download feed from: feedUrl")
                    // TODO send FeedStatusUpdate once we have the actorRef (cyclic dependency and such...)
                    // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            }
        }


    }

    // TODO this method must be updated to do all the pro crawling stuff (best using a decent HTTP client lib)
    private def download(feedUrl: String): String = return new Scanner(new URL(feedUrl).openStream, "UTF-8").useDelimiter("\\A").next



}
