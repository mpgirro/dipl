package echo.actor.store

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.protocol.Protocol._
import echo.core.dto.document.{EpisodeDocument, PodcastDocument}
import echo.core.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
class DirectoryStore (val crawler : ActorRef) extends Actor with ActorLogging {

    // k: feedUrl, v: (timestamp,status,[episodeIds])
    val database = scala.collection.mutable.Map.empty[String, (LocalDateTime,FeedStatus,scala.collection.mutable.Set[String])]

    override def receive: Receive = {

        case ProposeNewFeed(feedUrl) => {
            log.info("Received msg proposing a new feed: " + feedUrl)
            if(database.contains(feedUrl)){
                // TODO remove the auto update
                log.info("Feed already in directory; will send an update request to crawler")
                val entry = database(feedUrl)
                crawler ! FetchUpdateFeed(feedUrl, feedUrl, entry._3.toArray)
            } else {
                log.info("Feed not yet known; will be passed to crawler")
                val entry = (LocalDateTime.now(), FeedStatus.NEVER_CHECKED, scala.collection.mutable.Set[String]())
                database += (feedUrl -> entry)
                crawler ! FetchNewFeed(feedUrl, feedUrl)
            }
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {

            if(database.contains(feedUrl)){
                val entry = database(feedUrl)
                val newEntry = (timestamp, status, entry._3)
                database.updated(feedUrl, newEntry)
                log.info("Received FeedStatusUpdate: %s for %s", newEntry, feedUrl)
            } else {
                log.error("Received a FeedStatusUpdate for an unknown feed: " + feedUrl)
            }

        }

        case UpdatePodcastMetadata(docId: String, doc: PodcastDocument) => {
            // TODO I do not simulate podcasts in the DB yet
            log.warning("Received UpdatePodcastMetadata('%s')", docId)
            throw new UnsupportedOperationException("DirectoryStore does not yet support message UpdatePodcastMetadata")
        }

        case UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDocument) => {
            // TODO
            if(database.contains(podcastDocId)){
                log.info("Received UpdateEpisodeMetadata for podcast with docId: %s", podcastDocId)
                val entry = database(podcastDocId)
                val episodes = entry._3
                episodes += doc.getDocId
                val newEntry = (entry._1, entry._2, episodes)
                database.updated(podcastDocId, newEntry)
            } else {
                log.error("Received a UpdateEpisodeMetadata for an unknown podcast document claiming docId: %s", podcastDocId)
            }
        }

        case DebugPrintAllDatabase => {

            for( (k,v) <- database){
                println("docId: "+k+"\t"+v)
            }

        }


    }
}
