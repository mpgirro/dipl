package echo.actor.indexer

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import echo.actor.crawler.CrawlerActor
import echo.actor.protocol.Protocol._

class IndexerActor (val indexStore : ActorRef) extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[IndexerActor])

    override def receive: Receive = {

        /*
        case ProcessPodcastFeedData(feedData) => {
            log.info("Received ProcessPodcastFeedData('"+feedData+"') message")

            // TODO parse the feed data
            val podcast = feedData.split("/").last // TODO dummy to extract some data to index

            // TODO send messages to IndexRepo with the data to add/update to the index
            log.info("Sending AddPodcastToIndex('"+podcast+"') to IndexRepo")
            indexStore ! AddPodcastToIndex(podcast)

            // TODO generate some random episode data to have to process them too
            log.info("Sending ProcessEpisodeFeedData(...) to self:Indexer")
            self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#1")
            self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#2")
            self ! ProcessEpisodeFeedData(podcast,podcast+"-Episode-#3")
        }

        case ProcessEpisodeFeedData(feedRef, episode) => {
            log.info("Received ProcessEpisodeFeedData('"+feedRef+"','"+episode+"') message")

            // TODO this is a dummy processing of the episode data
            log.info("Sending AddEpisodeToIndex('"+feedRef+"','"+episode+"') to IndexRepo")
            indexStore ! AddEpisodeToIndex(feedRef, episode)
        }
        */




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
        }

        case IndexPodcastData(podcastDocId: String, podcastFeedData: String) => {
            // TODO when using a SAX parser, this would be most efficient by merging it with IndexFeedData
            /*
             * => indexStore ! IndexStoreAddPodcast(podcastDoc)
             * => indexStore ! IndexStoreUpdatePodcast(podcastDoc)
             */
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
        }


    }

}
