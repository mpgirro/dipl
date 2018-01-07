package echo.actor.store

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.protocol.Protocol._
import echo.core.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
class DirectoryStore (val crawler : ActorRef) extends Actor with ActorLogging {

    // k: feedUrl, v: (timestamp,status,[episodeIds])
    val database = scala.collection.mutable.HashMap.empty[String, (LocalDateTime,FeedStatus,List[String])]

    override def receive: Receive = {

        case ProposeNewFeed(feedUrl: String) => {
            log.info("Received msg proposing a new feed: " + feedUrl)
            if(database.contains(feedUrl)){
                // TODO remove the auto update
                log.info("Feed already in directory; will send an update request to crawler")
                val entry = database(feedUrl)
                crawler ! FetchUpdateFeed(feedUrl, feedUrl, entry._3.toArray)
            } else {
                log.info("Feed not yet known; will be passed to crawler")
                crawler ! FetchNewFeed(feedUrl, feedUrl)
            }
        }

        case FeedStatusUpdate(feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) => {

            if(database.contains(feedUrl)){
                val entry = database(feedUrl)
                val newEntry = (timestamp, status, entry._3)
                database.updated(feedUrl, newEntry)
                log.error("Received FeedStatusUpdate: %s for %s", newEntry, feedUrl)
            } else {
                log.error("Received a FeedStatusUpdate for an unknown feed: " + feedUrl)
            }

        }



    }
}
