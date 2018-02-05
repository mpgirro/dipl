package echo.actor.crawler

import java.io.{IOException, InputStreamReader}
import java.net.{HttpURLConnection, SocketTimeoutException, URL}
import java.time.LocalDateTime
import java.util.{Date, Scanner}

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import echo.actor.ActorProtocol._
import echo.core.exception.EchoException
import echo.core.model.feed.FeedStatus
import echo.core.parse.api.FyydAPI
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpGet, HttpHead, HttpRequestBase}
import org.apache.http.client.utils.DateUtils
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class CrawlerActor extends Actor with ActorLogging {

    val DOWNLOAD_TIMEOUT = 3 // TODO read from config

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _

    private val fyydAPI: FyydAPI = new FyydAPI()

    override def receive: Receive = {

        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref

        case FetchFeedForNewPodcast(url, podcastId) =>
            log.info("Received FetchFeedForNewPodcast('{}', {})", url, podcastId)

            try {
                val data = download(url)
                data match {
                    case Some(xml) =>
                        parser ! ParseNewPodcastData(url, podcastId, xml)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    case None =>
                        // log.error("Received NULL trying to download (new) feed from URL: {}", url)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            } catch {
                case e: IOException =>
                    log.error("IO Exception trying to download content from feed: {} [reason: {}]", url, e.getMessage)
                    directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }

        case FetchFeedForUpdateEpisodes(url, podcastId) =>
            log.info("Received FetchFeedForUpdateEpisodes('{}',{})", url, podcastId)

            try {
                val data = download(url)
                data match {
                    case Some(xml) =>
                        parser ! ParseEpisodeData(url, podcastId, xml)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    case None =>
                        // log.error("Received NULL trying to download (new) feed from URL: {}", url)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            } catch {
                case e: IOException =>
                    log.error("IO Exception trying to download content from feed: {} [reason: {}]", url, e.getMessage)
                    directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }

        case FetchWebsite(echoId, url) =>
            log.debug("Received FetchWebsite({},'{}')", echoId, url)

            try{
                val data = download(url)
                data match {
                    case Some(html) => parser ! ParseWebsiteData(echoId, html)
                    case None       => // we simply have no website data to add to the index --> ignore and move on
                        //log.error("Received NULL trying to download website data for URL: {}", url)
                }
            } catch {
                case e: IOException =>
                    log.error("IO Exception trying to download content from URL : {} [reason: {}]", url, e.getMessage)
                    directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }

        case CrawlFyyd(count) =>
            log.debug("Received CrawlFyyd({})", count)
            val feeds = fyydAPI.getFeedUrls(count)
            log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)

            log.debug("Proposing these feeds to the internal directory now")
            val it = feeds.iterator()
            while(it.hasNext){
                directoryStore ! ProposeNewFeed(it.next())
            }

        /*
        case HttpResponse(StatusCodes.OK, headers, entity, _) => {
            entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
                log.info("Got response, body: " + body.utf8String)
            }
        }

        case resp @ HttpResponse(StatusCodes.Redirection(_), headers, _, _) => {
            //Extract Location from headers and retry request with another pipeTo
            headers.foreach(h => println(h))
        }

        case resp @ HttpResponse(code, _, _, _) => {
            log.info("Request failed, response code: " + code)
            resp.discardEntityBytes()
        }
        */

    }

    private def buildHttpClient(): HttpClient = {
        val requestConfig = RequestConfig.custom.
            setSocketTimeout(DOWNLOAD_TIMEOUT * 1000)
            .setConnectTimeout(DOWNLOAD_TIMEOUT * 1000)
            .build
        HttpClientBuilder
            .create
            .setDefaultRequestConfig(requestConfig)
            //.setConnectionManager(poolingHttpClientConnectionManager)
            .build
    }

    private def executeRequestWithHardTimeout(request: HttpRequestBase): Option[HttpResponse] ={

        val httpClient = buildHttpClient()

        import context.dispatcher
        val requestTimeouter: Cancellable = context.system.scheduler.
            scheduleOnce(DOWNLOAD_TIMEOUT seconds) {
                if (request != null){
                    request.abort()
                }
            }

        val result = httpClient.execute(request)
        requestTimeouter.cancel()

        return if (request.isAborted) None else Some(result)
    }

    private def testHead(url: String): Try[(Option[String],Option[String],Option[Date])] = {

        var headMethod = new HttpHead(url)

        executeRequestWithHardTimeout(headMethod) match {
            case Some(httpResponse) =>
                val statusCode = httpResponse.getStatusLine.getStatusCode

                statusCode match {
                    case 200 => // all fine
                    case 404 => // not found: nothing there worth processing
                        return Failure(new EchoException(s"HEAD request reported status $statusCode : ${httpResponse.getStatusLine.getReasonPhrase}")) // TODO make a dedicated exception
                    case 503 => // service unavailable
                        return Failure(new EchoException(s"HEAD request reported status $statusCode : ${httpResponse.getStatusLine.getReasonPhrase}")) // TODO make a dedicated exception
                    case _   =>
                        log.warning("Received unexpected status={} from HEAD request on : {}", statusCode, url)
                }

                val mimeType: Option[String] = Option(httpResponse.getLastHeader("Content-Type"))
                    .map(contentTypeHeader => Some(contentTypeHeader.getValue.split(";")(0).trim))
                    .getOrElse(None)
                mimeType match {
                    case Some(mime) =>
                        mime match {
                            case "application/rss+xml"  => // feed
                            case "application/xml"      => // feed
                            case "text/xml"             => // feed
                            case "text/html"            => // website
                            case _@("audio/mpeg" | "application/octet-stream") =>
                                return Failure(new EchoException(s"Invalid MIME-type '$mime'")) // TODO make a dedicated exception
                            case _ =>
                                log.warning("Unexpected MIME type='{}' of response by : {}", mime, url)
                                return Failure(new EchoException(s"We do not process '$mime' (yet?)")) // TODO make a dedicated exception
                        }
                    case None =>
                        // got no content type from HEAD request, therefore I'll just have to download the whole thing and look for myself
                        log.warning("Did not get a Content-Type from HEAD request")
                }

                //set the new etag if existent
                val eTag: Option[String] = Option(httpResponse.getLastHeader("etag"))
                    .map(eTagHeader => Some(eTagHeader.getValue))
                    .getOrElse(None)

                //set the new "last modified" header field if existent
                val lastModified: Option[Date] = Option(httpResponse.getLastHeader("last-modified"))
                    .map(lastModifiedHeader => Some(DateUtils.parseDate(lastModifiedHeader.getValue)))
                    .getOrElse(None)

                // Release the connection.
                headMethod.releaseConnection()

                Success((mimeType, eTag, lastModified))
            case None =>
                Failure(new EchoException(s"Canceled HTTP request due to timeout on : {}", url))
        }
    }

    private def download(url: String): Option[String] = {

        try{
            testHead(url) match {
                case Success((mimeType,eTag,lastModified)) =>

                    // TODO check eTag if the resource has changed

                    // TODO check the lastModified date if the resource has changed

                    // val httpClient = buildHttpClient()

                    val getMethod = new HttpGet(url)

                    executeRequestWithHardTimeout(getMethod) match {
                        case Some(response) =>
                            val in = response.getEntity.getContent
                            val html = IOUtils.toString(new InputStreamReader(in))

                            return Option(html)
                        case None =>
                            log.warning("Canceled HTTP request due to timeout on: $url")
                            return None
                    }
                case Failure(reason) =>
                    log.error("HEAD request evaluation prevented downloading resource : {} [reason : {}]", url, Option(reason.getMessage).getOrElse("NON GIVEN IN EXCEPTION"))
                    return None
            }
        } catch {
            case e: java.net.UnknownHostException =>
                log.error("UnknownHostException for : {}", url)
                // TODO I should rethrow this exception, and then send a message that to mark the feed as invalid (dont crawl it again?)
                None
            case e: javax.net.ssl.SSLProtocolException =>
                log.error("SSLProtocolException for : {}", url)
                None
            case e: java.net.SocketException =>
                log.error("SocketException for : {}", url)
                None
            case e: org.apache.http.conn.ConnectTimeoutException =>
                log.error("ConnectTimeoutException for : {}", url)
                None
            case e: Exception =>
                e.printStackTrace()
                None
        }
    }

    // TODO do something useful with the future, e.g start a response handler child actor
    private def downloadAsync(url: String): Option[String] ={
        val httpclient: CloseableHttpAsyncClient = HttpAsyncClients.createDefault()
        // Start the client
        httpclient.start()

        // Execute request
        val request: HttpGet = new HttpGet(url)
        val future: java.util.concurrent.Future[HttpResponse] = httpclient.execute(request, null);

        // and wait until a response is received
        val response: HttpResponse = future.get()

        val in = response.getEntity.getContent
        val html: String = IOUtils.toString(new InputStreamReader(in))

        Option(html)
    }

}
