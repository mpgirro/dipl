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

    private var responseHandlerCounter = 0

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

            parse(podcastId, feedUrl, feedData, true)

            /*
            try {
                val podcast = Option(feedParser.parseFeed(feedData))
                podcast match {
                    case Some(p) =>
                        // TODO try-catch for Feedparseerror here, send update
                        // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

                        p.setEchoId(podcastId)

                        Option(p.getDescription).foreach(d => p.setDescription(Jsoup.clean(d, Whitelist.basic())))

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
            */

        case ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) =>
            log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)

            // checkForNewEpisodes(feedUrl, podcastId, episodeFeedData)
            parse(podcastId, feedUrl, episodeFeedData, false)

            log.debug("Finished ParseEpisodeData({},{},_)", feedUrl, podcastId)


        case ParseWebsiteData(echoId: String, html: String) =>
            log.debug("Received ParseWebsiteData({},_)", echoId)

            val readableText = Jsoup.parse(html).text()
            indexStore ! IndexStoreUpdateDocWebsiteData(echoId, readableText)

            log.debug("Finished ParseWebsiteData({},_)", echoId)

    }

    private def parse(podcastId: String, feedUrl: String, feedData: String, isNewPodcast: Boolean): Unit = {
        try {
            val podcast = Option(feedParser.parseFeed(feedData))
            podcast match {
                case Some(p) =>
                    // TODO try-catch for Feedparseerror here, send update
                    // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

                    p.setEchoId(podcastId)

                    Option(p.getDescription).foreach(d => p.setDescription(Jsoup.clean(d, Whitelist.basic())))

                    // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
                    directoryStore ! UpdatePodcastMetadata(podcastId, feedUrl, p)

                    if (isNewPodcast) {
                        indexStore ! IndexStoreAddDoc(IndexMapper.INSTANCE.map(p))

                        // request that the podcasts website will get added to the index as well, if possible
                        Option(p.getLink) match {
                            case Some(link) => crawler ! FetchWebsite(p.getEchoId, link)
                            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getEchoId)
                        }
                    }

                    // check for "new" episodes: because this is a new Podcast, all episodes will be new and registered
                    processEpisodes(feedUrl, podcastId, feedData)
                case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
            }
        } catch {
            case e: FeedParsingException =>
                log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                directoryStore ! FeedStatusUpdate(podcastId, feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
            case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
        }
    }

    private def processEpisodes(feedUrl: String, podcastId: String, feedData: String): Unit = {
        val episodes = Option(feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(feedData))
        episodes match {
            case Some(es) =>
                for(e <- es){

                    responseHandlerCounter += 1
                    val handler = context.actorOf(DirectoryStoreResponseHandler.props(podcastId, e, directoryStore, indexStore, crawler), s"${self.path.name}-response-handler-${responseHandlerCounter}")

                    directoryStore.tell(IsEpisodeRegistered(e.getEnclosureUrl, e.getEnclosureLength, e.getEnclosureType), handler)
                }
            case None => log.warning("Parsing generated a NULL-List[EpisodeDTO] for feed: {}", feedUrl)
        }
    }

    @Deprecated
    private def checkForNewEpisodes(feedUrl: String, podcastId: String, feedData: String): Unit = {
        try {
            val episodes = Option(feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(feedData))
            episodes match {
                case Some(es) =>
                    for(e <- es){

                        //val originalSender = Some(sender) // this is important to not expose the handler

                        responseHandlerCounter += 1
                        val handler = context.actorOf(DirectoryStoreResponseHandler.props(podcastId, e, directoryStore, indexStore, crawler), s"${self.path.name}-response-handler-${responseHandlerCounter}")

                        directoryStore.tell(IsEpisodeRegistered(e.getEnclosureUrl, e.getEnclosureLength, e.getEnclosureType), handler)
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



}
