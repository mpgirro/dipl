package echo.actor.indexer

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.protocol.ActorMessages._
import echo.core.exception.FeedParsingException
import echo.core.model.feed.FeedStatus
import echo.core.parse.rss.{FeedParser, RomeFeedParser}

class IndexerActor extends Actor with ActorLogging {

//    val log = Logging(context.system, classOf[IndexerActor])

    //val feedParser: FeedParser = new PodEngineFeedParser()
    val feedParser: FeedParser = new RomeFeedParser()

    private var indexStore: ActorRef = _
    private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _

    private var mockEchoIdGenerator = 0

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) => {
            log.debug("Received ActorRefIndexStoreActor")
            indexStore = ref
        }
        case ActorRefDirectoryStoreActor(ref) => {
            log.debug("Received ActorRefDirectoryStoreActor")
            directoryStore = ref
        }
        case ActorRefCrawlerActor(ref) => {
            log.debug("Received ActorRefCrawlerActor")
            crawler = ref
        }

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
            try {
                // TODO this is all highly test code

                val podcast = feedParser.parseFeed(feedData)

                if(podcast != null){
                    // TODO try-catch for Feedparseerror here, send update
                    // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

                    podcast.setEchoId(podcastDocId)
                    podcast.setDocId(podcastDocId)

                    /* TODO
                     * here I should send an update for the podcast data to the directoryStore (relational DB)
                     * In order to do this, I need the ActorRef for directoryStore, which results in a circular
                     * dependency. How am I supposed to solve this?
                     *
                     */
                    directoryStore ! UpdatePodcastMetadata(podcastDocId, podcast)

                    // send the document to the lucene index
                    indexStore ! IndexStoreAddPodcast(podcast)

                    // request that the website will get added to the index as well
                    if(podcast.getLink != null) {
                        crawler ! FetchWebsite(podcast.getEchoId, podcast.getLink)
                    }

                    val episodes = feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(feedData)

                    if(episodes != null){
                        for(episode <- episodes){

                            val fakeEpisodeId = "efake" + { mockEchoIdGenerator += 1; mockEchoIdGenerator }
                            episode.setEchoId(fakeEpisodeId)
                            episode.setDocId(fakeEpisodeId) // TODO verify good GUID!

                            // TODO send episode data to directoryStore, once the circular dependency is solved
                            directoryStore ! UpdateEpisodeMetadata(podcastDocId, episode)

                            indexStore ! IndexStoreAddEpisode(episode)


                            // if no iTunes artwork is set for this episode, communicate that the one of the while Podcast should be used
                            if(episode.getItunesImage == null || episode.getItunesImage.eq("")){
                                log.debug("Episodes itunesImage is not set -> sending message so that the Podcast's image will be used instead")
                                directoryStore ! UsePodcastItunesImage(episode.getEchoId)
                            }

                            // request that the website will get added to the index as well
                            if(episode.getLink != null) {
                                crawler ! FetchWebsite(episode.getEchoId, episode.getLink)
                            }
                        }
                    } else {
                        log.warning("Parsing generated a NULL-List[EpisodeDocument] for feed: {}", feedUrl)
                    }

                } else {
                    log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
                }
            } catch {
                case e: FeedParsingException => {
                    log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                    //log.error("FeedParsingException: {}", e)
                    directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                }
                case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
            }

        }

        case IndexPodcastData(podcastDocId: String, podcastFeedData: String) => {
            // TODO when using a SAX parser, this would be most efficient by merging it with IndexFeedData
            /*
             * => indexStore ! IndexStoreAddPodcast(podcastDoc)
             * => indexStore ! IndexStoreUpdatePodcast(podcastDoc)
             */

            log.error("Received IndexPodcastData for podcastDocId: " + podcastDocId)
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

            log.error("Received IndexEpisodeData for episodes: FORGET TO SET OUTPUT")
        }

        case IndexWebsiteData(echoId: String, websiteData: String) => {
            // TODO we don't to any processing of raw website source code yet
            indexStore ! IndexSoreUpdateDocumentWebsiteData(echoId, websiteData)
        }


    }

}
