package echo.actor.parser

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.devskiller.friendly_id.Url62
import echo.actor.ActorProtocol._
import echo.core.exception.FeedParsingException
import echo.core.model.feed.FeedStatus
import echo.core.parse.rss.{FeedParser, RomeFeedParser}

class ParserActor extends Actor with ActorLogging {

    private val feedParser: FeedParser = new RomeFeedParser()

    private var indexStore: ActorRef = _
    private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _

    private var mockEchoIdGenerator = 0

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor")
            indexStore = ref
        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor")
            directoryStore = ref
        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor")
            crawler = ref

        case ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String) =>
            log.debug("Received ParseNewPodcastData for feed: " + feedUrl)
            try {
                val podcast = Option(feedParser.parseFeed(feedData))
                podcast match {
                    case Some(p) =>
                        // TODO try-catch for Feedparseerror here, send update
                        // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

                        p.setEchoId(podcastId)

                        /* TODO
                         * do the same as with episodes, directory will have to save out if we already have the podcast in the database
                         * and only if not, we will add it to the index
                         */

                        directoryStore ! UpdatePodcastMetadata(podcastId, feedUrl, p)

                        indexStore ! IndexStoreAddPodcast(p)

                        // request that the website will get added to the index as well
                        Option(p.getLink) match {
                            case Some(link) => crawler ! FetchWebsite(p.getEchoId, link)
                            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getEchoId)
                        }
                    case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
                }
            } catch {
                case e: FeedParsingException =>
                    log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                    directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
            }

        case ParsePodcastData(podcastId: String, podcastFeedData: String) =>
            // TODO when using a SAX parser, this would be most efficient by merging it with IndexFeedData
            /*
             * => indexStore ! IndexStoreAddPodcast(podcastDoc)
             * => indexStore ! IndexStoreUpdatePodcast(podcastDoc)
             */

            log.error("Received ParsePodcastData for podcastDocId: " + podcastId)

        case ParseEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) =>
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

            /* TODO
             * for now, episodeFeedData contains the whole feed in the string. eventually it should be turned into an
             * array of strings, with each string holding the <item>...</item> XML data of each episode, extracted by a
             * SAX parser (that has not yet processed the episode data)
             */

            log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)
            try {
                val episodes = Option(feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(episodeFeedData))
                episodes match {
                    case Some(es) =>
                        for(e <- es){

                            //val fakeEpisodeId = "efake" + { mockEchoIdGenerator += 1; mockEchoIdGenerator }
                            val fakeEpisodeId =  Url62.encode(UUID.randomUUID())
                            e.setEchoId(fakeEpisodeId)

                            /* TODO
                             * for now, we always send the update episode and add to index messages, but eventually
                             * we'll have to find out weither the episode is already known, (directory will have to say).
                             * then we either do not update the episode (or we just do), and we will add to index only
                             * of it is a new episode
                             *
                             * --> for starters, do not update in either directory nor index, for performance, only add new ones
                             */
                            directoryStore ! UpdateEpisodeMetadata(podcastId, e)

                            indexStore ! IndexStoreAddEpisode(e)

                            // request that the website will get added to the index as well
                            Option(e.getLink) match {
                                case Some(link) => crawler ! FetchWebsite(e.getEchoId, link)
                                case None => log.debug("No link set for episode {} --> no website data will be added to the index", e.getEchoId)
                            }
                        }
                    case None => log.warning("Parsing generated a NULL-List[EpisodeDTO] for feed: {}", feedUrl)
                }
            } catch {
                case e: FeedParsingException =>
                    log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                    directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
            }


        case ParseWebsiteData(echoId: String, html: String) =>
            // TODO we don't to any processing of raw website source code yet
            log.debug("Received ParseWebsiteData({},_)", echoId)

            import org.jsoup.Jsoup
            val readableText = Jsoup.parse(html).text()

            indexStore ! IndexStoreUpdateDocWebsiteData(echoId, readableText)

    }

}
