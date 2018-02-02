package echo.actor.crawler

import java.io.IOException
import java.net.{HttpURLConnection, SocketTimeoutException, URL}
import java.time.LocalDateTime
import java.util.Scanner

import akka.actor.{Actor, ActorLogging, ActorRef}
import echo.actor.ActorProtocol._
import echo.core.model.feed.FeedStatus
import echo.core.parse.api.FyydAPI

class CrawlerActor extends Actor with ActorLogging {

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

        case FetchNewFeed(url, podcastId) =>
            log.info("Received FetchNewFeed('{}')", url)

            try {
                val data = download(url)
                data match {
                    case Some(xml) =>
                        parser ! ParseFeedData(url, podcastId, xml)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    case None => log.error("Received NULL trying to download (new) feed from URL: {}", url)
                }
            } catch {
                case e: IOException =>
                    log.error("IO Exception trying to download content from feed: {} [reason: {}]", url, e.getMessage)
                    directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }

        case FetchUpdateFeed(url, podcastId) =>

            // TODO NewFeed und UpdateFeed unterscheiden sich noch kaum

            log.info("Received FetchUpdateFeed('{}')", url)
            try {
                val data = download(url)
                data match {
                    case Some(xml) =>
                        parser ! ParseFeedData(url, podcastId, xml)
                        directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    case None =>
                        log.error("Received NULL trying to download feed (update)from URL: {}", url)
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
                    case None       => log.error("Received NULL trying to download website data for URL: {}", url)
                }
            } catch {
                case e: IOException =>
                    log.error("IO Exception trying to download content from URL: {} [reason: {}]", url, e.getMessage)
                    directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }

        case CrawlFyyd(count) =>
            log.debug("Received CrawlFyyd({})", count)
            val feeds = fyydAPI.getFeedUrls(count);
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
            case e: SocketTimeoutException => {
                log.error("Timout while loading src from URL: {} [reason: {}]", url, e.getMessage)
            }
            case e: java.lang.StackOverflowError => {
                log.error("StackOverflowError trying to download: {} [reason: {}]", url, e.getMessage)
            }
        }
        None
    }

}
