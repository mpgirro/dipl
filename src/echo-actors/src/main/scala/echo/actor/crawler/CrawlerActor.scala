package echo.actor.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.scaladsl.SourceQueueWithComplete
import com.softwaremill.sttp._
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.domain.feed.FeedStatus
import echo.core.exception.EchoException
import echo.core.http.HttpClient
import echo.core.parse.api.FyydAPI

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Promise}
import scala.language.postfixOps
import scala.compat.java8.OptionConverters._

/**
  * @author Maximilian Irro
  */
class CrawlerActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WEBSITE_JOBS: Boolean = Option(CONFIG.getBoolean("echo.crawler.website-jobs")).getOrElse(false)

    // TODO define CONN_TIMEOUT
    // TODO define READ_TIMEOUT

    private val DOWNLOAD_TIMEOUT = 10.seconds // TODO read from config
    private val DOWNLOAD_MAXBYTES = 5  * 1024 * 1024 // TODO load from config file

    // important, or we will experience starvation on processing many feeds at once
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

    private implicit val actorSystem: ActorSystem = context.system
    private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

    private implicit val sttpBackend = HttpURLConnectionBackend(options = SttpBackendOptions.connectionTimeout(DOWNLOAD_TIMEOUT))

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _
    private var indexStore: ActorRef = _

    private val fyydAPI: FyydAPI = new FyydAPI()

    private val httpClient: HttpClient = new HttpClient(DOWNLOAD_TIMEOUT)

    override def postStop: Unit = {
        sttpBackend.close()
        httpClient.close()
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


        case DownloadContent(echoId, url, job) =>
            log.debug("Received Download({},'{}',{})", echoId, url, job.getClass.getSimpleName)
            fetchContent(echoId, url, job)

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

    private def analyzeUrl(url: String): (String, String) = {

        // http, https, ftp if provided
        val protocol = if (url.indexOf("://") > -1) {
            url.split("://")(0)
        } else {
            ""
        }

        val hostname = {
            if (url.indexOf("://") > -1)
                url.split('/')(2)
            else
                url.split('/')(0)
            }
            .split(':')(0) // find & remove port number
            .split('?')(0) // find & remove "?"
            // .split('/')(0) // find & remove the "/" that might still stick at the end of the hostname

        (hostname, protocol)
    }

    private def isValidMime(mime: String): Boolean = {
        mime match {
            case "application/rss+xml"      => true // feed
            case "application/xml"          => true // feed
            case "text/xml"                 => true // feed
            case "text/html"                => true // website
            case "text/plain"               => true // might be ok and might be not -> will have to check manually
            case "none/none"                => true // might be ok and might be not -> will have to check manually
            case "application/octet-stream" => true // some sites use this, but might also be used for media files
            case _                          => false
        }
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
            /*
            val response = emptyRequest // use empty request, because standard req uses header "Accept-Encoding: gzip" which can cause problems with HEAD requests
                .response(ignore)
                .readTimeout(DOWNLOAD_TIMEOUT)
                .head(uri"${url}")
                .send()

            // TODO
            var saveToDownload = true

            // we assume we will use the known URL to download later, but maybe this changes...
            var location: Option[String] = Some(url)
            response.code match {
                case 200 => // all fine
                case 301 => // Moved Permanently
                    // TODO do something with the new location, e.g. send message to directory to update episode, and use this to (re-)index the new website
                    location = response.header("location")
                    log.debug("Redirecting {} to {}", url, location.getOrElse("NON PROVIDED"))
                //log.warning("301 Moved Permanently reported, this is the new location : {} (of : {})", location.getOrElse("NON PROVIDED"), url)
                // TODO once I have a propper procedure for 301 handling, should I fail here?
                //return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
                case 302 => // odd, but ok
                case 404 => // not found: nothing there worth processing
                    //return Failure(new EchoException(s"HEAD request reported status ${response.code} : ${response.statusText}")) // TODO make a dedicated exception
                    log.warning("HEAD request reported status {} : {}", response.code, response.statusText)
                    saveToDownload = false
                case 503 => // service unavailable
                    //return Failure(new EchoException(s"HEAD request reported status ${response.code} : ${response.statusText}")) // TODO make a dedicated exception
                    log.warning("HEAD request reported status {} : {}", response.code, response.statusText)
                    saveToDownload = false
                case _   =>
                    log.warning("Received unexpected status from HEAD request : {} {} on {}", response.code, response.statusText, url)
            }

            val mimeType: Option[String] = response.contentType
                .map(ct => Some(ct.split(";")(0).trim))
                .getOrElse(None)

            mimeType match {
                case Some(mime) =>
                    if (!isValidMime(mime)) {
                        mime match {
                            case _@("audio/mpeg" | "application/octet-stream") =>
                                //return Failure(new EchoException(s"Invalid MIME-type '$mime' of $url")) // TODO make a dedicated exception
                                log.warning("Invalid MIME-type '{}' of '{}'", mime, url)
                                saveToDownload = false
                            case _ =>
                                //log.warning("Received unexpected MIME type '{}' from HEAD request to : '{}'", mime, url)
                                //return Failure(new EchoException(s"Unexpected MIME type '$mime' of '$url'")) // TODO make a dedicated exception
                                log.error("Unexpected MIME type '{}' of '{}'", mime, url)
                                saveToDownload = false
                        }
                    }
                case None =>
                    // got no content type from HEAD request, therefore I'll just have to download the whole thing and look for myself
                    log.warning("Did not get a Content-Type from HEAD request")
            }

            if (saveToDownload) {
                //set the etag if existent
                val eTag: Option[String] = response.header("etag")

                // TODO check if eTag differs from last known value

                //set the "last modified" header field if existent
                val lastModified: Option[String] = response.header("last-modified")

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
                                self ! DownloadContent(exo, href, job)
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
                                self ! DownloadContent(exo, href, job)
                        }
                    case None =>
                        log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                        sendErrorNotificationIfFeasable(exo, url, job)
                }
            }
            */

            val headResult = httpClient.headCheck(url)

            // TODO check if eTag differs from last known value

            // TODO check if lastMod differs from last known value

            headResult.getLocation.asScala match {
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
                            self ! DownloadContent(exo, href, job)
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
                            self ! DownloadContent(exo, href, job)
                    }
                case None =>
                    log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                    sendErrorNotificationIfFeasable(exo, url, job)
            }


        } catch {
            case e: Exception =>
                log.warning("HEAD response prevented fetching resource : {} [reason : {}]", url, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
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
    private def fetchContent(exo: String, url: String, job: FetchJob): Unit = {
        try {
            val response = sttp
                .get(uri"${url}")
                .readTimeout(DOWNLOAD_TIMEOUT)
                .response(asString)
                .send()

            if (!response.isSuccess) {
                //log.error("Download resulted in a non-success response code : {}", response.code)
                throw new EchoException(s"Download resulted in a non-success response code : ${response.code}") // TODO make dedicated exception
            }

            response.contentType.foreach(ct => {
                val mimeType = ct.split(";")(0).trim
                if (!isValidMime(mimeType)) {
                    //log.error("Aborted before downloading a file with invalid MIME-type : '{}' from : '{}'", mimeType, url)
                    throw new EchoException(s"Aborted before downloading a file with invalid MIME-type : '${mimeType}'") // TODO make dedicated exception
                }
            })

            response.contentLength.foreach(cl => {
                if (cl > DOWNLOAD_MAXBYTES) {
                    //log.error("Refusing to download resource because content length exceeds maximum: {} > {}", cl, DOWNLOAD_MAXBYTES)
                    throw new EchoException(s"Refusing to download resource because content length exceeds maximum: ${cl} > ${DOWNLOAD_MAXBYTES}")
                }
            })

            // TODO
            response.body match {
                case Left(errorMessage) =>
                    //log.error("Error collecting download body, message : {}", errorMessage)
                    throw new EchoException(s"Error collecting download body, message : ${errorMessage}") // TODO make dedicated exception
                case Right(deserializedBody) =>
                    log.debug("Finished collecting content from GET response : {}", url)
                    job match {
                        case NewPodcastFetchJob() =>
                            parser ! ParseNewPodcastData(url, exo, deserializedBody)
                            directoryStore ! FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                        case UpdateEpisodesFetchJob(etag, lastMod) =>
                            parser ! ParseUpdateEpisodeData(url, exo, deserializedBody)
                            directoryStore ! FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                        case WebsiteFetchJob() =>
                            parser ! ParseWebsiteData(exo, deserializedBody)
                    }
            }
        } catch {
            case e: Exception =>
                // TODO
                log.error("Error fetching content : {} [reason : {}]", url, e.getMessage)
                //throw new EchoException(s"Error downloading resource : ${url} [reason : ${reason.getMessage}]") // TODO make dedicated exception
                sendErrorNotificationIfFeasable(exo, url, job)
        }
    }

}
