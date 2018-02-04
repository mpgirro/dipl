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
                val data = download_v2(url)
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
                val data = download_v2(url)
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
                val data = download_v2(url)
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

    /**
      * Returns the text (content) from a REST URL as a String.
      * Inspired by http://matthewkwong.blogspot.com/2009/09/scala-scalaiosource-fromurl-blockshangs.html
      * and http://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java
      *
      * The `connectTimeout` and `readTimeout` comes from the Java URLConnection class Javadoc.
      * The connection follows HTTP redirects
      * @param url The full URL to connect to.
      * @param connectTimeout Sets a specified timeout value, in milliseconds,
      * to be used when opening a communications link to the resource referenced
      * by this URLConnection. If the timeout expires before the connection can
      * be established, a java.net.SocketTimeoutException
      * is raised. A timeout of zero is interpreted as an infinite timeout.
      * Defaults to 5000 ms.
      * @param readTimeout If the timeout expires before there is data available
      * for read, a java.net.SocketTimeoutException is raised. A timeout of zero
      * is interpreted as an infinite timeout. Defaults to 5000 ms.
      * @param requestMethod Defaults to "GET". (Other methods have not been tested.)
      *
      * @example get("http://www.example.com/getInfo")
      * @example get("http://www.example.com/getInfo", 5000)
      * @example get("http://www.example.com/getInfo", 5000, 5000)
      * @throws IOException
      */
    @throws(classOf[IOException])
    private def download(url: String,
                         connectTimeout: Int = 5000,
                         readTimeout: Int = 5000,
                         requestMethod: String = "GET"): Option[String] = {

        try {
            val u = new URL(url)
            var conn = u.openConnection.asInstanceOf[HttpURLConnection]
            HttpURLConnection.setFollowRedirects(false)
            conn.setConnectTimeout(connectTimeout)
            conn.setReadTimeout(readTimeout)
            conn.setRequestMethod(requestMethod)
            //conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");

            // request URL and see what happens
            conn.connect()

            var redirect = false
            var notFound = false

            // normally, 3xx is redirect
            val status = conn.getResponseCode
            status match {
                case HttpURLConnection.HTTP_OK         => /* all is well */
                case HttpURLConnection.HTTP_MOVED_TEMP => redirect = true;
                case HttpURLConnection.HTTP_MOVED_PERM => redirect = true;
                case HttpURLConnection.HTTP_SEE_OTHER  => redirect = true;
                case HttpURLConnection.HTTP_NOT_FOUND  => notFound = true;
                case _ => log.warning("Unhandeled status received from download: {}", status)
            }

            // if we've got a 404, there is no point going on
            if (notFound) {
                return None
            }

            if (redirect) {
                // get redirect url from "location" header field
                val newUrl = conn.getHeaderField("Location")

                // get the cookie if need, for login
                val cookies = conn.getHeaderField("Set-Cookie")

                // open the new connnection again
                conn = new URL(newUrl).openConnection().asInstanceOf[HttpURLConnection]
                conn.setConnectTimeout(connectTimeout)
                conn.setReadTimeout(readTimeout)
                conn.setRequestMethod(requestMethod)
                conn.setRequestProperty("Cookie", cookies)
                //conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla")
            }

            val scanner = new Scanner(conn.getInputStream, "UTF-8").useDelimiter("\\A")
            if(scanner.hasNext){
                return Some(scanner.next)
            }
        } catch {
            case e: SocketTimeoutException =>
                log.error("Timout while loading src from URL: {} [reason: {}]", url, e.getMessage)
            case e: java.lang.StackOverflowError =>
                log.error("StackOverflowError trying to download: {} [reason: {}]", url, e.getMessage)
        }
        None
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

    private def download_v2(url: String): Option[String] = {

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
