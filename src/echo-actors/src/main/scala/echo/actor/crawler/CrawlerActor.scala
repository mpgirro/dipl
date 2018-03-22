package echo.actor.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.stream._
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.directory.DirectoryProtocol.{FeedStatusUpdate, ProposeNewFeed, UpdateFeedUrl, UpdateLinkByEchoId}
import echo.actor.index.IndexProtocol.IndexStoreUpdateDocLink
import echo.core.domain.feed.FeedStatus
import echo.core.exception.EchoException
import echo.core.http.HttpClient
import echo.core.parse.api.FyydAPI

import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
class CrawlerActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WEBSITE_JOBS: Boolean = Option(CONFIG.getBoolean("echo.crawler.website-jobs")).getOrElse(false)

    // TODO define CONN_TIMEOUT
    // TODO define READ_TIMEOUT

    private val DOWNLOAD_TIMEOUT = 10 // TODO read from config
    private val DOWNLOAD_MAXBYTES = 5242880 // = 5  * 1024 * 1024 // TODO load from config file

    // important, or we will experience starvation on processing many feeds at once
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

    private implicit val actorSystem: ActorSystem = context.system
    private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _
    private var indexStore: ActorRef = _

    private val fyydAPI: FyydAPI = new FyydAPI()

    private val httpClient: HttpClient = new HttpClient(DOWNLOAD_TIMEOUT, DOWNLOAD_MAXBYTES)

    private var currUrl: String = _
    private var currJob: FetchJob = _

    override def postStop: Unit = {
        httpClient.close()
    }

    override def postRestart(cause: Throwable): Unit = {
        // TODO
        super.postRestart(cause)
        log.info("I was restarted")
        cause match {
            case e: EchoException =>
                log.error("HEAD response prevented fetching resource : {} [reason : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
            case e: java.net.ConnectException =>
                log.error("java.net.ConnectException for HEAD check on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
            case e: java.net.SocketTimeoutException =>
                log.error("java.net.SocketTimeoutException for HEAD check on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
            case e: java.net.UnknownHostException =>
                log.error("java.net.UnknownHostException for HEAD check on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
            case e: javax.net.ssl.SSLHandshakeException =>
                log.error("javax.net.ssl.SSLHandshakeException for HEAD check on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
            case e: Exception =>
                // TODO
                log.error("Unhandled Exception on {} : {}", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                e.printStackTrace()
        }
    }

    override def receive: Receive = {

        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref

        case ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref

        case DownloadWithHeadCheck(echoId, url, job) =>

            this.currUrl = url
            this.currJob = job

            job match {
                case WebsiteFetchJob() =>
                    if (WEBSITE_JOBS) {
                        log.info("Received DownloadWithHeadCheck({}, '{}', {})", echoId, url, job.getClass.getSimpleName)
                        headCheck(echoId, url, job)
                    }
                case _ =>
                    log.info("Received DownloadWithHeadCheck({}, '{}', {})", echoId, url, job.getClass.getSimpleName)
                    headCheck(echoId, url, job)
            }


        case DownloadContent(echoId, url, job, encoding) =>
            log.debug("Received Download({},'{}',{},{})", echoId, url, job.getClass.getSimpleName, encoding)

            this.currUrl = url
            this.currJob = job

            fetchContent(echoId, url, job, encoding) // TODO send encoding via message

        case CrawlFyyd(count) => onCrawlFyyd(count)

        case LoadFyydEpisodes(podcastId, fyydId) => onLoadFyydEpisodes(podcastId, fyydId)

    }

    private def onCrawlFyyd(count: Int) = {
        log.debug("Received CrawlFyyd({})", count)

        val feeds = fyydAPI.getFeedUrls(count)

        log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)
        log.debug("Proposing these feeds to the internal directory now")

        val it = feeds.iterator()
        while (it.hasNext) {
            directoryStore ! ProposeNewFeed(it.next())
        }
    }

    private def onLoadFyydEpisodes(podcastId: String, fyydId: Long) = {
        log.debug("Received LoadFyydEpisodes({},'{}')", podcastId, fyydId)

        val json = fyydAPI.getEpisodesByPodcastIdJSON(fyydId)
        parser ! ParseFyydEpisodes(podcastId, json)
    }

    private def sendErrorNotificationIfFeasable(echoId: String, url: String, job: FetchJob): Unit = {
        job match {
            case WebsiteFetchJob() => // do nothing...
            case _ =>
                directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
        }
    }

    private def headCheck(exo: String, url: String, job: FetchJob): Unit = {

        try {

            val headResult = httpClient.headCheck(url)

            val encoding = headResult.getContentEncoding.asScala

            val location = headResult.getLocation.asScala

            // TODO check if eTag differs from last known value

            // TODO check if lastMod differs from last known value

            location match {
                case Some(href) =>
                    log.debug("Sending message to download content : {}", href)
                    job match {
                        case WebsiteFetchJob() =>
                            // if the link in the feed is redirected (which is often the case due
                            // to some feed analytic tools, we set our records to the new location
                            if (!url.equals(href)) {
                                directoryStore ! UpdateLinkByEchoId(exo, href)
                                indexStore ! IndexStoreUpdateDocLink(exo, href)
                            }

                            // we always download websites, because we only do it once anyway
                            self ! DownloadContent(exo, href, job, encoding) // TODO
                        //fetchContent(exo, href, job, encoding)

                        case _ =>
                            // if the feed moved to a new URL, we will inform the directory, so
                            // it will use the new location starting with the next update cycle
                            if (!url.equals(href)) {
                                directoryStore ! UpdateFeedUrl(url, href)
                            }

                            /*
                             * TODO
                             * here I have to do some voodoo with etag/lastMod to
                             * determine weither the feed changed and I really need to redownload
                             */
                            self ! DownloadContent(exo, href, job, encoding) // TODO
                        //fetchContent(exo, href, job, encoding)
                    }
                case None =>
                    log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                    sendErrorNotificationIfFeasable(exo, url, job)
            }

        } catch {
            case e: EchoException =>
                log.error("HEAD response prevented fetching resource : {} [reason : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                sendErrorNotificationIfFeasable(exo, url, job)
            case e: java.net.ConnectException =>
                log.error("java.net.ConnectException for HEAD check on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: java.net.SocketTimeoutException =>
                log.error("java.net.SocketTimeoutException for HEAD check on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: java.net.UnknownHostException =>
                log.error("java.net.UnknownHostException for HEAD check on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: javax.net.ssl.SSLHandshakeException =>
                log.error("javax.net.ssl.SSLHandshakeException for HEAD check on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: Exception =>
                // TODO
                log.error("Unhandled Exception during HEAD on {} : {}", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                e.printStackTrace()
                sendErrorNotificationIfFeasable(exo, url, job)
        }

    }

    /**
      *
      * Docs for STTP: http://sttp.readthedocs.io/en/latest/
      *
      * @param exo
      * @param url
      * @param job
      */
    private def fetchContent(exo: String, url: String, job: FetchJob, encoding: Option[String]): Unit = {

        try {

            val data = httpClient.fetchContent(url, encoding.asJava)
            job match {
                case NewPodcastFetchJob() =>
                    parser ! ParseNewPodcastData(url, exo, data)
                    directoryStore ! FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)

                case UpdateEpisodesFetchJob(etag, lastMod) =>
                    parser ! ParseUpdateEpisodeData(url, exo, data)
                    directoryStore ! FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)

                case WebsiteFetchJob() =>
                    parser ! ParseWebsiteData(exo, data)
            }

        } catch {
            case e: EchoException =>
                log.error("Error on fetching resource : {} [reason : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                sendErrorNotificationIfFeasable(exo, url, job)
            case e: java.net.ConnectException =>
                log.error("java.net.ConnectException for fetch on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: java.net.SocketTimeoutException =>
                log.error("java.net.SocketTimeoutException for HEAD check on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: java.net.UnknownHostException =>
                log.error("java.net.UnknownHostException for fetch on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: javax.net.ssl.SSLHandshakeException =>
                log.error("javax.net.ssl.SSLHandshakeException for fetch on : {} [msg : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                //sendErrorNotificationIfFeasable(exo, url, job) // TODO
            case e: Exception =>
                // TODO
                log.error("Unhandled Exception during GET on {} : {}", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                e.printStackTrace()
                sendErrorNotificationIfFeasable(exo, url, job)
        }

    }

}
