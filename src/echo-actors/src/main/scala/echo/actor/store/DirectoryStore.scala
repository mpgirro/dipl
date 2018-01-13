package echo.actor.store

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.EchoApp.indexStore
import echo.actor.protocol.ActorMessages._
import echo.core.dto.document.{EpisodeDocument, PodcastDocument}
import echo.core.feed.FeedStatus

/**
  * @author Maximilian Irro
  */

class DirectoryStore (val crawler : ActorRef) extends Actor with ActorLogging {

    // k: feedUrl, v: (timestamp,status,[episodeIds], itunesImage)
    val database = scala.collection.mutable.Map.empty[String, (LocalDateTime,FeedStatus,scala.collection.mutable.Set[String], String)]

    private var indexStore: ActorRef = _

    override def receive: Receive = {

        case ActorRefIndexStoreActor(indexStore) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            this.indexStore = indexStore
        }

        case ProposeNewFeed(feedUrl) => {
            log.debug("Received msg proposing a new feed: " + feedUrl)
            if(database.contains(feedUrl)){
                // TODO remove the auto update
                log.debug("Feed already in directory; will send an update request to crawler: {}", feedUrl)
                val entry = database(feedUrl)
                crawler ! FetchUpdateFeed(feedUrl, feedUrl, entry._3.toArray)
            } else {
                log.debug("Feed not yet known; will be passed to crawler: {}", feedUrl)
                val entry = (LocalDateTime.now(), FeedStatus.NEVER_CHECKED, scala.collection.mutable.Set[String](), "")
                database += (feedUrl -> entry)
                crawler ! FetchNewFeed(feedUrl, feedUrl)
            }
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {

            if(database.contains(feedUrl)){
                log.debug("Received FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
                val entry = database(feedUrl)
                val newEntry = (timestamp, status, entry._3, entry._4)

                // note: database.updated(...) does not work for some reason
                database.remove(feedUrl)
                database += (feedUrl -> newEntry)
            } else {
                log.error("Received UNKNOWN FEED FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
            }

        }

        case UpdatePodcastMetadata(docId: String, doc: PodcastDocument) => {
            log.debug("Received UpdatePodcastMetadata({},{})", docId, doc)

            // TODO for now, we only set the itunesImage in the fake database
            if(database.contains(docId)){
                val e = database(docId)
                val newEntry = (e._1,e._2,e._3,doc.getItunesImage)
                database += (docId -> newEntry)
            }
        }

        case UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDocument) => {
            // TODO
            if(database.contains(podcastDocId)){
                log.debug("Received UpdateEpisodeMetadata({},{})", podcastDocId, doc)
                val entry = database(podcastDocId)
                val episodes = entry._3
                episodes += doc.getDocId
                val newEntry = (entry._1, entry._2, episodes, entry._4)

                database.remove(podcastDocId)
                database += (podcastDocId -> newEntry)
            } else {
                log.error("Received a UpdateEpisodeMetadata for an unknown podcast document claiming docId: %s", podcastDocId)
            }
        }

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => {
            log.info("Received UsePodcastItunesImage({})", echoId)
            var found = false

            for((_,(_,_,episodes,itunesImage)) <- database){
                if(episodes.contains(echoId)){
                    indexStore ! IndexStoreUpdateEpisodeAddItunesImage(echoId,itunesImage)
                }
            }

            if(found){
                log.info("Did not find echoId={} in the database (could not set its itunesImage therefore)", echoId)
            }
        }

        case DebugPrintAllDatabase => {
            log.debug("Received DebugPrintAllDatabase")
            for( (k,v) <- database){
                println("docId: "+k+"\t"+v)
            }
        }


    }
}
