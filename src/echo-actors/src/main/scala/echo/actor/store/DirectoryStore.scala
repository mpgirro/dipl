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

    // feedUrl -> (timestamp, status, [episodeIds], PodcastDTO)
    val podcastDB = scala.collection.mutable.Map.empty[String, (LocalDateTime,FeedStatus,scala.collection.mutable.Set[String], PodcastDTO)]

    // echoId -> (EpisodeDTO)
    val episodeDB = scala.collection.mutable.Map.empty[String, EpisodeDTO]

    private var indexStore: ActorRef = _

    private var mockEchoIdGenerator = 0 // TODO replace with real ID gen

    override def receive: Receive = {

        case ActorRefIndexStoreActor(indexStore) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            this.indexStore = indexStore
        }

        case ProposeNewFeed(feedUrl) => {
            log.debug("Received msg proposing a new feed: " + feedUrl)

            val fakePodcastId = "fakePc" + { mockEchoIdGenerator += 1; mockEchoIdGenerator }

            if(podcastDB.contains(fakePodcastId)){
                // TODO remove the auto update
                log.debug("Feed already in directory; will send an update request to crawler: {}", feedUrl)
                val (_,_,episodes,_) = podcastDB(fakePodcastId)
                crawler ! FetchUpdateFeed(feedUrl, fakePodcastId, episodes.toArray)
            } else {
                log.debug("Feed not yet known; will be passed to crawler: {}", feedUrl)
                val entry = (LocalDateTime.now(), FeedStatus.NEVER_CHECKED, scala.collection.mutable.Set[String](), null)
                podcastDB += (fakePodcastId -> entry)
                crawler ! FetchNewFeed(feedUrl, fakePodcastId)
            }
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {

            if(podcastDB.contains(feedUrl)){
                log.debug("Received FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
                val (_, _, episodes, podcast) = podcastDB(feedUrl)
                val newEntry = (timestamp, status, episodes, podcast)

                // note: database.updated(...) does not work for some reason
                podcastDB.remove(feedUrl)
                podcastDB += (feedUrl -> newEntry)
            } else {
                log.error("Received UNKNOWN FEED FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
            }

        }

        case UpdatePodcastMetadata(docId: String, podcast: PodcastDTO) => {
            log.debug("Received UpdatePodcastMetadata({},{})", docId, podcast)

            // TODO for now, we only set the itunesImage in the fake database
            if(podcastDB.contains(docId)){
                val (timestamp, status, episodes, _) = podcastDB(docId)
                val newEntry = (timestamp, status, episodes, podcast)
                podcastDB += (docId -> newEntry)
            }
        }

        case UpdateEpisodeMetadata(podcastDocId: String, episode: EpisodeDTO) => {
            // TODO
            if(podcastDB.contains(podcastDocId)){
                log.debug("Received UpdateEpisodeMetadata({},{})", podcastDocId, episode)
                val (timestamp, status, episodes, podcast) = podcastDB(podcastDocId)
                episodes += episode.getDocId
                val newEntry = (timestamp, status, episodes, podcast)

                podcastDB.remove(podcastDocId)
                podcastDB += (podcastDocId -> newEntry)
            } else {
                log.error("Received a UpdateEpisodeMetadata for an unknown podcast document claiming docId: {}", podcastDocId)
            }

            /* TODO we just overwrite everything here
            var episodeEntry: EpisodeDocument = null
            if(episodeDB.contains((doc.getDocId))){
                episodeEntry = episodeDB(doc.getDocId)
            } else {
                episodeEntry = doc;
            }
            */
            episodeDB += (episode.getDocId -> episode)
        }

        // this is the case when an Episode has no iTunesImage URL set in the feed.
        // then we should set the image url of the whole podcast
        case UsePodcastItunesImage(echoId) => {
            log.debug("Received UsePodcastItunesImage({})", echoId)
            var found = false

            for((_,(_,_,episodes,podcast)) <- podcastDB){
                if(episodes.contains(echoId)){
                    indexStore ! IndexStoreUpdateEpisodeAddItunesImage(echoId,podcast.getItunesImage)
                }
            }

            if(found){
                log.info("Did not find echoId={} in the database (could not set its itunesImage therefore)", echoId)
            }
        }


        case GetPodcast(echoId) => {
            log.debug("Received GetPodcast('{}')", echoId)

            if(podcastDB.contains(echoId)){
                sender ! PodcastResult(podcastDB(echoId)._4)
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
