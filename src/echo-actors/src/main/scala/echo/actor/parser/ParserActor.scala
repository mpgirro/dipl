package echo.actor.parser

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, IOException}
import java.net.URL
import java.time.LocalDateTime
import java.util.Base64
import javax.imageio.ImageIO

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.mortennobel.imagescaling.ResampleOp
import echo.actor.ActorProtocol._
import echo.actor.directory.DirectoryProtocol.{FeedStatusUpdate, RegisterEpisodeIfNew, UpdatePodcast}
import echo.actor.index.IndexProtocol.{IndexStoreAddDoc, IndexStoreUpdateDocWebsiteData}
import echo.core.domain.dto.EpisodeDTO
import echo.core.domain.feed.FeedStatus
import echo.core.exception.FeedParsingException
import echo.core.mapper.{EpisodeMapper, IndexMapper, PodcastMapper}
import echo.core.parse.api.FyydAPI
import echo.core.parse.rss.{FeedParser, RomeFeedParser}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object ParserActor {
    def name(workerIndex: Int): String = "worker-" + workerIndex
    def props(): Props = Props(new ParserActor()).withDispatcher("echo.parser.dispatcher")
}

class ParserActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val indexMapper = IndexMapper.INSTANCE

    private val feedParser: FeedParser = new RomeFeedParser()
    private val fyydAPI: FyydAPI = new FyydAPI()

    private var indexStore: ActorRef = _
    private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ParseNewPodcastData(feedUrl: String, podcastExo: String, feedData: String) =>
            log.debug("Received ParseNewPodcastData for feed: " + feedUrl)

            parse(podcastExo, feedUrl, feedData, isNewPodcast = true)

            log.debug("Finished ParseNewPodcastData({},{},_)", feedUrl, podcastExo)

        case ParseUpdateEpisodeData(feedUrl: String, podcastExo: String, episodeFeedData: String) =>
            log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastExo)

            parse(podcastExo, feedUrl, episodeFeedData, isNewPodcast = false)

            log.debug("Finished ParseEpisodeData({},{},_)", feedUrl, podcastExo)


        case ParseWebsiteData(exo: String, html: String) =>
            log.debug("Received ParseWebsiteData({},_)", exo)

            val readableText = Jsoup.parse(html).text()
            indexStore ! IndexStoreUpdateDocWebsiteData(exo, readableText)

            log.debug("Finished ParseWebsiteData({},_)", exo)

        case ParseFyydEpisodes(podcastExo, json) =>
            log.debug("Received ParseFyydEpisodes({},_)", podcastExo)

            val episodes: List[EpisodeDTO] = fyydAPI.getEpisodes(json).asScala.toList
            log.info("Loaded {} episodes from fyyd for podcast : {}", episodes.size, podcastExo)
            for(episode <- episodes){
                registerEpisode(podcastExo, episode)
            }

            log.debug("Finished ParseFyydEpisodes({},_)", podcastExo)

    }

    private def parse(podcastExo: String, feedUrl: String, feedData: String, isNewPodcast: Boolean): Unit = {
        try {
            val podcast = Option(feedParser.parseFeed(feedData))
            podcast match {
                case Some(pcst) =>
                    val p = podcastMapper.toModifiable(pcst)
                    // TODO try-catch for Feedparseerror here, send update
                    // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

                    p.setExo(podcastExo)

                    Option(p.getTitle).foreach(t => p.setTitle(t.trim))
                    Option(p.getDescription).foreach(d => p.setDescription(Jsoup.clean(d, Whitelist.basic())))

                    if (isNewPodcast) {

                        /* TODO
                        // experimental: this works but has terrible performance and assumes we have a GUI app
                        Option(p.getItunesImage).foreach(img => {
                            p.setItunesImage(base64Image(img))
                        })
                        */

                        indexStore ! IndexStoreAddDoc(indexMapper.toImmutable(p))

                        // request that the podcasts website will get added to the index as well, if possible
                        Option(p.getLink) match {
                            case Some(link) =>
                                // crawler ! FetchWebsite(p.getEchoId, link)
                                crawler ! DownloadWithHeadCheck(p.getExo, link, WebsiteFetchJob())
                            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getExo)
                        }
                    }

                    // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
                    directoryStore ! UpdatePodcast(podcastExo, feedUrl, p.toImmutable)

                    // check for "new" episodes: because this is a new Podcast, all episodes will be new and registered
                    processEpisodes(feedUrl, podcastExo, feedData)
                case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
            }
        } catch {
            case e: FeedParsingException =>
                log.error("FeedParsingException occured while processing feed: {}", feedUrl)
                directoryStore ! FeedStatusUpdate(podcastExo, feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
            case e: java.lang.StackOverflowError => log.error("StackOverflowError parsing: {}", feedUrl)
        }
    }

    private def processEpisodes(feedUrl: String, podcastExo: String, feedData: String): Unit = {
        val episodes = Option(feedParser.asInstanceOf[RomeFeedParser].extractEpisodes(feedData).asScala)
        episodes match {
            case Some(es) =>
                for(e <- es){
                    registerEpisode(podcastExo, e)
                }
            case None => log.warning("Parsing generated a NULL-List[EpisodeDTO] for feed: {}", feedUrl)
        }
    }

    private def registerEpisode(podcastExo: String, episode: EpisodeDTO): Unit = {

        val e = episodeMapper.toModifiable(episode)

        // cleanup some potentially markuped texts
        Option(e.getTitle).foreach(t => e.setTitle(t.trim))
        Option(e.getDescription).foreach(d => e.setDescription(Jsoup.clean(d, Whitelist.basic())))
        Option(e.getContentEncoded).foreach(c => e.setContentEncoded(Jsoup.clean(c, Whitelist.basic())))

        /* TODO
        // experimental: this works but has terrible performance and assumes we have a GUI app
        Option(e.getItunesImage).foreach(img => {
            e.setItunesImage(base64Image(img))
        })
        */

        directoryStore ! RegisterEpisodeIfNew(podcastExo, e.toImmutable)
    }

    private def base64Image(imageUrl: String): String = {
        try {
            val sourceImage: BufferedImage = ImageIO.read(new URL(imageUrl))
            if(sourceImage == null) return null
            val resampleOp: ResampleOp = new ResampleOp(400,400)
            val scaledImage = resampleOp.filter(sourceImage, null)
            val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
            ImageIO.write(scaledImage, "jpg", outputStream)
            val base64 = Base64.getEncoder.encodeToString(outputStream.toByteArray)
            "data:image/png;base64," + base64
        } catch {
            case e: IOException =>
                null
        }
    }

}
