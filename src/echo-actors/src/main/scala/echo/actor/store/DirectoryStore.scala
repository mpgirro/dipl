package echo.actor.store

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.EchoApp.indexStore
import echo.actor.protocol.ActorMessages._
import echo.core.dto.document.{EpisodeDTO, PodcastDTO}
import echo.core.feed.FeedStatus

/**
  * @author Maximilian Irro
  */

class DirectoryStore (val crawler : ActorRef) extends Actor with ActorLogging {

    // feedUrl -> (timestamp, status, [episodeIds], itunesImage)
    val podcastDB = scala.collection.mutable.Map.empty[String, (LocalDateTime,FeedStatus,scala.collection.mutable.Set[String], String)]

    // echoId -> (itunesImage)
    val episodeDB = scala.collection.mutable.Map.empty[String, EpisodeDTO]

    private var indexStore: ActorRef = _

    override def receive: Receive = {

        case ActorRefIndexStoreActor(indexStore) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            this.indexStore = indexStore
        }

        case ProposeNewFeed(feedUrl) => {
            log.debug("Received msg proposing a new feed: " + feedUrl)
            if(podcastDB.contains(feedUrl)){
                // TODO remove the auto update
                log.debug("Feed already in directory; will send an update request to crawler: {}", feedUrl)
                val entry = podcastDB(feedUrl)
                crawler ! FetchUpdateFeed(feedUrl, feedUrl, entry._3.toArray)
            } else {
                log.debug("Feed not yet known; will be passed to crawler: {}", feedUrl)
                val entry = (LocalDateTime.now(), FeedStatus.NEVER_CHECKED, scala.collection.mutable.Set[String](), "")
                podcastDB += (feedUrl -> entry)
                crawler ! FetchNewFeed(feedUrl, feedUrl)
            }
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {

            if(podcastDB.contains(feedUrl)){
                log.debug("Received FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
                val entry = podcastDB(feedUrl)
                val newEntry = (timestamp, status, entry._3, entry._4)

                // note: database.updated(...) does not work for some reason
                podcastDB.remove(feedUrl)
                podcastDB += (feedUrl -> newEntry)
            } else {
                log.error("Received UNKNOWN FEED FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
            }

        }

        case UpdatePodcastMetadata(docId: String, doc: PodcastDTO) => {
            log.debug("Received UpdatePodcastMetadata({},{})", docId, doc)

            // TODO for now, we only set the itunesImage in the fake database
            if(podcastDB.contains(docId)){
                val e = podcastDB(docId)
                val newEntry = (e._1,e._2,e._3,doc.getItunesImage)
                podcastDB += (docId -> newEntry)
            }
        }

        case UpdateEpisodeMetadata(podcastDocId: String, doc: EpisodeDTO) => {
            // TODO
            if(podcastDB.contains(podcastDocId)){
                log.debug("Received UpdateEpisodeMetadata({},{})", podcastDocId, doc)
                val entry = podcastDB(podcastDocId)
                val episodes = entry._3
                episodes += doc.getDocId
                val newEntry = (entry._1, entry._2, episodes, entry._4)

                podcastDB.remove(podcastDocId)
                podcastDB += (podcastDocId -> newEntry)
            } else {
                log.error("Received a UpdateEpisodeMetadata for an unknown podcast document claiming docId: %s", podcastDocId)
            }

            /* TODO we just overwrite everything here
            var episodeEntry: EpisodeDocument = null
            if(episodeDB.contains((doc.getDocId))){
                episodeEntry = episodeDB(doc.getDocId)
            } else {
                episodeEntry = doc;
            }
            */
            episodeDB += (doc.getDocId -> doc)
        }

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => {
            log.debug("Received UsePodcastItunesImage({})", echoId)
            var found = false

            for((_,(_,_,episodes,itunesImage)) <- podcastDB){
                if(episodes.contains(echoId)){
                    indexStore ! IndexStoreUpdateEpisodeAddItunesImage(echoId,itunesImage)
                }
            }

            if(found){
                log.info("Did not find echoId={} in the database (could not set its itunesImage therefore)", echoId)
            }
        }


        case GetPodcast(echoId) => {
            log.debug("Received GetPodcast('{}')", echoId)

            if(podcastDB.contains(echoId)){
                // TODO
                throw new UnsupportedOperationException("GetPodcast(_) not yet supported!")
                //sender ! PodcastResult(podcastDB(echoId))
            } else {
                log.error("Database does not contain Podcast with echoId={}", echoId)
                sender ! NoDocumentFound(echoId)
            }
        }

        case GetEpisode(echoId) => {
            log.debug("Received GetEpisode('{}')", echoId)
            if(episodeDB.contains(echoId)){
                sender ! EpisodeResult(episodeDB(echoId))
            } else {
                log.error("Database does not contain Episode with echoId={}", echoId)
                sender ! NoDocumentFound(echoId)
            }
        }

        case DebugPrintAllDatabase => {
            log.debug("Received DebugPrintAllDatabase")
            for( (k,v) <- podcastDB){
                println("docId: "+k+"\t"+v)
            }
        }


    }
}
