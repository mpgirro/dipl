package echo.actor.parser

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Send}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.catalog.CatalogBroker
import echo.actor.catalog.CatalogProtocol._
import echo.actor.index.IndexProtocol.{AddDocIndexEvent, IndexEvent, UpdateDocWebsiteDataIndexEvent}
import echo.core.benchmark.mps.MessagesPerSecondMeter
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto.EpisodeDTO
import echo.core.domain.feed.FeedStatus
import echo.core.exception.FeedParsingException
import echo.core.mapper.{EpisodeMapper, IndexMapper, PodcastMapper}
import echo.core.parse.api.FyydAPI
import echo.core.parse.rss.RomeFeedParser
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object ParserWorker {
    def name(workerIndex: Int): String = "worker-" + workerIndex
    def props(): Props = Props(new ParserWorker()).withDispatcher("echo.parser.dispatcher")
}

class ParserWorker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val indexMapper = IndexMapper.INSTANCE

    private val catalogEventStream = CONFIG.getString("echo.catalog.event-stream")
    private val indexEventStream = CONFIG.getString("echo.index.event-stream")
    private val mediator = DistributedPubSub(context.system).mediator

    //private var directoryStore: ActorRef = _
    private var crawler: ActorRef = _
    private var benchmarkMonitor: ActorRef = _
    private var supervisor: ActorRef = _

    private val mpsMeter = new MessagesPerSecondMeter(self.path.toStringWithoutAddress)
    private val fyydAPI: FyydAPI = new FyydAPI()

    private var currFeedUrl = ""
    private var currPodcastExo = ""

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: FeedParsingException =>
                log.error("FeedParsingException occured while processing feed : {}", currFeedUrl)
                //directoryStore ! FeedStatusUpdate(currPodcastExo, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                val catalogEvent = FeedStatusUpdate(currPodcastExo, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
                emitCatalogEvent(catalogEvent)
                currPodcastExo = ""
                currFeedUrl = ""
            case e: java.lang.StackOverflowError =>
                log.error("StackOverflowError parsing : {} ; reason: {}", currFeedUrl, e.getMessage, e)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref

        case ActorRefBenchmarkMonitor(ref) =>
            log.debug("Received ActorRefBenchmarkMonitor(_)")
            benchmarkMonitor = ref

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case StartMessagePerSecondMonitoring =>
            log.debug("Received StartMessagePerSecondMonitoring(_)")
            mpsMeter.startMeasurement()

        case StopMessagePerSecondMonitoring =>
            log.debug("Received StopMessagePerSecondMonitoring(_)")
            mpsMeter.stopMeasurement()
            benchmarkMonitor ! MessagePerSecondReport(mpsMeter.getResult)
            supervisor ! ChildMpsReport(mpsMeter.getResult)

        case ParseNewPodcastData(feedUrl: String, podcastExo: String, feedData: String, rtt: RoundTripTime) =>
            log.debug("Received ParseNewPodcastData for feed: " + feedUrl)
            mpsMeter.tick()

            currFeedUrl = feedUrl
            currPodcastExo = podcastExo

            parse(podcastExo, feedUrl, feedData, isNewPodcast = true, rtt)

            currFeedUrl = ""
            currPodcastExo = ""

        case ParseUpdateEpisodeData(feedUrl: String, podcastExo: String, episodeFeedData: String, rtt: RoundTripTime) =>
            log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastExo)
            mpsMeter.tick()

            currFeedUrl = feedUrl
            currPodcastExo = podcastExo

            parse(podcastExo, feedUrl, episodeFeedData, isNewPodcast = false, rtt)

            currFeedUrl = ""
            currPodcastExo = ""

        case ParseWebsiteData(exo: String, html: String) =>
            log.debug("Received ParseWebsiteData({},_)", exo)
            mpsMeter.tick()

            val readableText = Jsoup.parse(html).text()

            val indexEvent = UpdateDocWebsiteDataIndexEvent(exo, readableText)
            mediator ! Publish(indexEventStream, indexEvent)

        case ParseFyydEpisodes(podcastExo, json) =>
            log.debug("Received ParseFyydEpisodes({},_)", podcastExo)
            mpsMeter.tick()

            val episodes: List[EpisodeDTO] = fyydAPI.getEpisodes(json).asScala.toList
            log.info("Loaded {} episodes from fyyd for podcast : {}", episodes.size, podcastExo)
            for(episode <- episodes){
                registerEpisode(podcastExo, episode, RoundTripTime.empty())
            }

    }

    private def sendCatalogCommand(command: CatalogCommand): Unit = {
        mediator ! Send("/user/node/"+CatalogBroker.name, command, localAffinity = true)
    }

    private def emitCatalogEvent(event: CatalogEvent): Unit = {
        mediator ! Publish(catalogEventStream, event)
    }

    private def emitIndexEvent(event: IndexEvent): Unit = {
        mediator ! Publish(indexEventStream, event)
    }

    private def parse(podcastExo: String, feedUrl: String, feedData: String, isNewPodcast: Boolean, rtt: RoundTripTime): Unit = {

        val parser = RomeFeedParser.of(feedData)
        Option(parser.getPodcast) match {
            case Some(podcast) =>
                val p = podcastMapper.toModifiable(podcast)
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

                    val indexEvent = AddDocIndexEvent(indexMapper.toImmutable(p), rtt.bumpRTTs())
                    emitIndexEvent(indexEvent)

                    // request that the podcasts website will get added to the index as well, if possible
                    Option(p.getLink) match {
                        case Some(link) =>
                            crawler ! DownloadWithHeadCheck(p.getExo, link, WebsiteFetchJob(), RoundTripTime.empty())
                        case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getExo)
                    }
                }

                // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
                val catalogEvent = UpdatePodcast(podcastExo, feedUrl, p.toImmutable)
                emitCatalogEvent(catalogEvent)

                // check for "new" episodes: because this is a new Podcast, all episodes will be new and registered
                Option(parser.getEpisodes) match {
                    case Some(es) =>
                        for(e <- es.asScala){
                            registerEpisode(podcastExo, e, rtt)
                        }
                    case None => log.warning("Parsing generated a NULL-List[EpisodeDTO] for feed: {}", feedUrl)
                }
            case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
        }
    }

    private def registerEpisode(podcastExo: String, episode: EpisodeDTO, rtt: RoundTripTime): Unit = {

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

        val catalogCommand = RegisterEpisodeIfNew(podcastExo, e.toImmutable, rtt.bumpRTTs())
        sendCatalogCommand(catalogCommand)
    }

    /* TODO this code works but produces bad output and is super slow!
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
    */

}
