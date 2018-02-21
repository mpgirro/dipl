package echo.actor.parser

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.ActorProtocol._
import echo.core.domain.dto.{DTO, EpisodeDTO}
import echo.core.domain.feed.FeedStatus
import echo.core.exception.FeedParsingException
import echo.core.mapper.IndexMapper
import echo.core.parse.rss.{FeedParser, RomeFeedParser}
import echo.core.util.EchoIdGenerator
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

class ParserActor extends Actor with ActorLogging {

    log.info("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val feedParser: FeedParser = new RomeFeedParser()

    private var indexStore: ActorRef = _
    private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _

    override def postStop: Unit = {
        log.info(s"${self.path.name} shut down")
    }

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

                        Option(p.getDescription).foreach(d => p.setDescription(Jsoup.clean(d, Whitelist.basic())))

                        /* TODO
                         * do the same as with episodes, directory will have to save out if we already have the podcast in the database
                         * and only if not, we will add it to the index
                         */

                        directoryStore ! UpdatePodcastMetadata(podcastId, feedUrl, p)
                        indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(p))

                        // request that the website will get added to the index as well
                        Option(p.getLink) match {
                            case Some(link) => crawler ! FetchWebsite(p.getEchoId, link)
                            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getEchoId)
                        }

                        // check for "new" episodes: because this is a new Podcast, all episodes will be new and registered
                        checkForNewEpisodes(feedUrl, podcastId, feedData)
                    case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
                }
            } catch {
                case e: FeedParsingException =>
                    log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                    directoryStore ! FeedStatusUpdate(podcastId, feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
            }

            log.debug("Finished ParseNewPodcastData for feed: " + feedUrl)

        case ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) =>
            log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)

            checkForNewEpisodes(feedUrl, podcastId, episodeFeedData)

            log.debug("Finished ParseEpisodeData({},{},_)", feedUrl, podcastId)


        case ParseWebsiteData(echoId: String, html: String) =>
            log.debug("Received ParseWebsiteData({},_)", echoId)

            val readableText = Jsoup.parse(html).text()
            indexStore ! IndexStoreUpdateDocWebsiteData(echoId, readableText)

            log.debug("Finished ParseWebsiteData({},_)", echoId)

    }

    private def checkForNewEpisodes(feedUrl: String, podcastId: String, feedData: String): Unit = {
        try {
            val episodes = Option(feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(feedData))
            episodes match {
                case Some(es) =>
                    for(e <- es){

                        // TODO check if the episode is already known.
                        registerNewEpisode(podcastId, e)

                    }
                case None => log.warning("Parsing generated a NULL-List[EpisodeDTO] for feed: {}", feedUrl)
            }
        } catch {
            case e: FeedParsingException =>
                log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                directoryStore ! FeedStatusUpdate(podcastId, feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
            case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
        }
    }

    private def registerNewEpisode(podcastId: String, episode: EpisodeDTO): Unit = {

        // generate a new episode echoId - the generator is (almost) ensuring uniqueness
        episode.setEchoId(EchoIdGenerator.getNewId)

        // cleanup some potentially markuped texts
        Option(episode.getDescription).foreach(d => episode.setDescription(Jsoup.clean(d, Whitelist.basic())))
        Option(episode.getContentEncoded).foreach(c => episode.setContentEncoded(Jsoup.clean(c, Whitelist.basic())))

        // send out the episode data for for registration/indexation
        directoryStore ! UpdateEpisodeMetadata(podcastId, episode)
        indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(episode))

        // request that the website will get added to the episodes index entry as well
        Option(episode.getLink) match {
            case Some(link) => crawler ! FetchWebsite(episode.getEchoId, link)
            case None => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getEchoId)
        }
    }

}
