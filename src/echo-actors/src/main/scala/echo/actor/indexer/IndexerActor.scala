package echo.actor.indexer

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.crawler.CrawlerActor
import echo.actor.protocol.Protocol._
import echo.core.parse.{FeedParser, PodEngineFeedParser}

class IndexerActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[IndexerActor])

    val feedParser: FeedParser = new PodEngineFeedParser()

    override def receive: Receive = {

        /*
         * received from Crawler
         */
        case IndexFeedData(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String], feedData: String) => {

            /* Notes
             * - the podcastDocId has to be there (originally generated from FeedStore, even for new feeds)
             * - the episodeDocIds contain only existing Episode IDs, and may therefore be empty (for new feeds)
             */

            /* TODO
             * - parse the raw feed data as XML
             * - extract the general info, and send a IndexPodcastData message to indexer
             * - extract episode data, and send a IndexEpisodeData **for each episode separately**
             * - a SAX parser would be best for performance. the podcast data could be processed directly, and episode messages generated on the fly when encountering the specific xml-tags
             */

            log.debug("Received IndexFeedData for feed: " + feedUrl)

            // TODO this is all highly test code
            val podcastDocument = feedParser.parseFeed(feedData)
            podcastDocument.setDocId(podcastDocId)

            /* TODO
             * here I should send an update for the podcast data to the directoryStore (relational DB)
             * In order to do this, I need the ActorRef for directoryStore, which results in a circular
             * dependency. How am I supposed to solve this?
             * directoryStore ! UpdatePodcastMetadata(podcastDocId, podcastDocument)
             */

            // send the document to the lucene index
            indexStore ! IndexStoreAddPodcast(podcastDocument)

            val episodes = feedParser.asInstanceOf[PodEngineFeedParser].extractEpisodes(feedData)
            for(episode <- episodes){
                episode.setDocId(episode.getGuid) // TODO verify good GUID!

                // TODO send episode data to directoryStore, once the circular dependency is solved
                // directoryStore ! UpdateEpisodeMetadata(podcastDocId, episode)

                indexStore ! IndexStoreAddEpisode(episode)
            }

        }

        case IndexPodcastData(podcastDocId: String, podcastFeedData: String) => {
            // TODO when using a SAX parser, this would be most efficient by merging it with IndexFeedData
            /*
             * => indexStore ! IndexStoreAddPodcast(podcastDoc)
             * => indexStore ! IndexStoreUpdatePodcast(podcastDoc)
             */

            log.debug("Received IndexPodcastData for podcastDocId: " + podcastDocId)
        }

        case IndexEpisodeData(episodeDocIds: Array[String], episodeFeedData: String) => {
            /* TODO
             * - process the XML data (this could be used with a DOM parser (!)
             * - if the episodes GUID is contained in the known episodeDocIds, the the episode must not be processed (simply end and do not generate a new message)
             * - if the episodes GUID is NOT contained in the episodeDocIds
             *  - use the GUID of the episode XML as new episodeDocId (performe a quality check!)
             *    - if the embedded feed GUID is bad quality, we need to generate on
             *      it must be reproducable
             *      it must be quick
             *      it must be send to the index, but also to the FeedStore (marked as a generate one); maybe do this by sending as complex message to someone else that could to this better? even feedstore?
             *
             * => add/update message to IndexStore with document (add is separate, because slower due to the additionaly delete involved (which may fail!)
             * indexStore ! IndexStoreAddEpisode(episodeDoc)
             * indexStore ! IndexStoreUpdateEpisode(episodeDoc)
             */

            log.debug("Received IndexEpisodeData for episodes: FORGET TO SET OUTPUT")
        }


    }

}
