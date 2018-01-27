package echo.actor.store

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.protocol.ActorMessages._
import echo.core.dto.{EpisodeDTO, PodcastDTO}
import echo.core.model.feed.FeedStatus

/**
  * @author Maximilian Irro
  */

class DirectoryStore extends Actor with ActorLogging {

    // podcastId -> (timestamp, status, [episodeIds], PodcastDTO)
    private val podcastDB = scala.collection.mutable.Map.empty[String, (LocalDateTime,FeedStatus,scala.collection.mutable.Set[String], PodcastDTO)]

    // episodeId -> (EpisodeDTO)
    private val episodeDB = scala.collection.mutable.Map.empty[String, EpisodeDTO]

    // TODO for now, podcasts only have one single feed
    // podcastId -> FeedURLs
    private val feedDB = scala.collection.mutable.Map.empty[String, String]

    private var crawler: ActorRef = _
    private var indexStore: ActorRef = _

    private var mockEchoIdGenerator = 0 // TODO replace with real ID gen

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) => {
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
        }
        case ActorRefIndexStoreActor(ref) => {
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
        }

        case ProposeNewFeed(feedUrl) => {
            log.debug("Received msg proposing a new feed: " + feedUrl)

            //val fakePodcastId = "pfake" + { mockEchoIdGenerator += 1; mockEchoIdGenerator }
            val fakePodcastId = Url62.encode(UUID.randomUUID())

            /*
            Url62.create
            // 7NLCAyd6sKR7kDHxgAWFPG

            Url62.decode("7NLCAyd6sKR7kDHxgAWFPG")
            // c3587ec5-0976-497f-8374-61e0c2ea3da5

            Url62.encode(UUID.fromString("c3587ec5-0976-497f-8374-61e0c2ea3da5"))
            */


            if(podcastDB.contains(fakePodcastId)){
                // TODO remove the auto update
                log.debug("Feed already in directory; will send an update request to crawler: {}", feedUrl)
                val (_,_,episodes,_) = podcastDB(fakePodcastId)
                crawler ! FetchUpdateFeed(feedUrl, fakePodcastId, episodes.toArray)
            } else {
                log.debug("Feed not yet known; will be passed to crawler: {}", feedUrl)

                // create a new entry in PodcastDB
                val podcastEntry = (LocalDateTime.now(), FeedStatus.NEVER_CHECKED, scala.collection.mutable.Set[String](), null)
                podcastDB += (fakePodcastId -> podcastEntry)

                // create a new entry in FeedDB
                val feedEntry = feedUrl
                feedDB += (fakePodcastId -> feedEntry)

                // do this at the end, to be sure that the database contains already the entries!
                // TODO hier bin ich vielleicht zu abhängig davon das die werte schon in der datenbank stehen!
                crawler ! FetchNewFeed(feedUrl, fakePodcastId)
            }
        }

        case FeedStatusUpdate(feedUrl, timestamp, status) => {

            var success = false;

            for( (podcastId,f) <- feedDB){
                if(f.equals(feedUrl)){
                    log.debug("Received FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
                    if(podcastDB.contains(podcastId)){
                        val (_, _, episodes, podcast) = podcastDB(podcastId)
                        val newEntry = (timestamp, status, episodes, podcast)

                        // note: database.updated(...) does not work for some reason
                        podcastDB.remove(podcastId)
                        podcastDB += (podcastId -> newEntry)

                        success = true
                    } else {
                        log.error(s"Found a Podcast id=$podcastId in FeedDB, but it could not be found in PodcastDB")
                    }
                }
            }

            if(!success){
                log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", feedUrl, timestamp, status)
            }
        }

        case UpdatePodcastMetadata(docId: String, podcast: PodcastDTO) => {
            log.debug("Received UpdatePodcastMetadata({},{})", docId, podcast)

            // TODO for now, we only set the itunesImage in the fake database
            if(podcastDB.contains(docId)){
                val (timestamp, status, episodes, _) = podcastDB(docId)
                val newEntry = (timestamp, status, episodes, podcast)
                podcastDB += (docId -> newEntry)
            } else {
                log.error("Received a UpdatePodcastMetadata for an unknown podcast document claiming docId: {}", docId)
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

                    // first ('cause concurrency 'n stuff) we send along the notification to update the image in the index
                    indexStore ! IndexStoreUpdateEpisodeAddItunesImage(echoId,podcast.getItunesImage)

                    // then we update the info in our own local database
                    if(episodeDB.contains(echoId)){
                        val episode = episodeDB(echoId)
                        episode.setItunesImage(podcast.getItunesImage)
                    } else {
                        log.error("Episode echoId={} was found as a member of a podcast, but is not present in the Episode table")
                    }
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

        case GetAllPodcasts => {
            log.debug("Received GetAllPodcasts()")

            var results = scala.collection.mutable.ArrayBuffer.empty[PodcastDTO]
            for((_,_,_,podcast: PodcastDTO) <- podcastDB.values){
                results += podcast
            }

            sender ! AllPodcastsResult(results.toArray)
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

        case GetEpisodesByPodcast(podcastId) => {
            log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

            if(podcastDB.contains(podcastId)){

                val (_,_,episodes,_) = podcastDB(podcastId)
                var results = scala.collection.mutable.ArrayBuffer.empty[EpisodeDTO]

                for(episodeId <- episodes){
                    if(episodeDB.contains(episodeId)){
                        results += episodeDB(episodeId)
                    } else {
                        log.error(s"Podcast Table entry for podcast=$podcastId references an Episode with echoId=$episodeId , but Episode Table has no entry for this id")
                    }
                }

                sender ! EpisodesByPodcastResult(results.toArray)
            } else {
                log.error("Database does not contain Podcast with echoId={}", podcastId)
                sender ! NoDocumentFound(podcastId)
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
