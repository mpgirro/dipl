package echo.actor.crawler

import java.io.IOException
import java.net.{HttpURLConnection, SocketTimeoutException, URL}
import java.time.LocalDateTime
import java.util.Scanner

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import akka.util.ByteString
import echo.actor.protocol.ActorMessages._
import echo.core.feed.FeedStatus
import echo.core.parse.api.FyydAPI

import scala.concurrent.{ExecutionContext, Future}

class CrawlerActor (val indexer: ActorRef) extends Actor with ActorLogging {

    //val log = Logging(context.system, classOf[CrawlerActor])

    private var directoryStore: ActorRef = _

    val fyydAPI: FyydAPI = new FyydAPI();

    /*
    import akka.pattern.pipe
    import context.dispatcher

    final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
    val http = Http(context.system)
    */

    override def receive: Receive = {

        case ActorRefDirectoryStoreActor(ref) => {
            log.debug("Received ActorRefDirectoryStoreActor")
            directoryStore = ref;
        }

        case FetchNewFeed(feedUrl: String, podcastDocId: String) => {

            log.info("Received FetchNewFeed({})", feedUrl)
            try {
                val feedData = download(feedUrl)

                // send downloaded data to Indexer for processing
                indexer ! IndexFeedData(feedUrl, podcastDocId, Array.empty, feedData)

                // send status to DirectoryStore
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } catch {
                case e: IOException => {
                    log.error("Could not download feed from: {}", feedUrl)
                    directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                }
            }
        }

        case FetchUpdateFeed(feedUrl: String, podcastDocId: String, episodeDocIds: Array[String]) => {

            log.info("Received FetchUpdateFeed({})", feedUrl)
            val feedData = download(feedUrl)
            if(feedData != null){
                // send downloaded data to Indexer for processing
                indexer ! IndexFeedData(feedUrl, podcastDocId, episodeDocIds, feedData)

                // send status to DirectoryStore
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
            } else {
                log.error("Could not download feed from: {}", feedUrl)
                directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
            }
        }

        case FetchWebsite(echoId: String, url: String) => {
            log.info("Received FetchWebsite({},{})", echoId, url)

            val websiteData = download(url)
            if(websiteData != null){
                indexer ! IndexWebsiteData(echoId, websiteData)
            }
        }

        case CrawlFyyd(count) => {
            log.debug("Received CrawlFyyd({})", count)
            val feeds = fyydAPI.getFeedUrls(count);
            log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)

            log.debug("Proposing these feeds to the internal directory now")
            val it = feeds.iterator()
            while(it.hasNext){
                directoryStore ! ProposeNewFeed(it.next())
            }
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
      */
    private def download(url: String,
                         connectTimeout: Int = 5000,
                         readTimeout: Int = 5000,
                         requestMethod: String = "GET"): String = {

        try {
            val u = new URL(url)
            var conn = u.openConnection.asInstanceOf[HttpURLConnection]
            HttpURLConnection.setFollowRedirects(false)
            conn.setConnectTimeout(connectTimeout)
            conn.setReadTimeout(readTimeout)
            conn.setRequestMethod(requestMethod)
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");

            // request URL and see what happens
            conn.connect

            var redirect = false;

            // normally, 3xx is redirect
            val status = conn.getResponseCode;
            status match {
                case HttpURLConnection.HTTP_OK         => {  /* all is well */ }
                case HttpURLConnection.HTTP_MOVED_TEMP => redirect = true;
                case HttpURLConnection.HTTP_MOVED_PERM => redirect = true;
                case HttpURLConnection.HTTP_SEE_OTHER  => redirect = true;
            }

            if (redirect) {

                // get redirect url from "location" header field
                val newUrl = conn.getHeaderField("Location");

                // get the cookie if need, for login
                val cookies = conn.getHeaderField("Set-Cookie");

                // open the new connnection again
                conn = new URL(newUrl).openConnection().asInstanceOf[HttpURLConnection];
                conn.setConnectTimeout(connectTimeout)
                conn.setReadTimeout(readTimeout)
                conn.setRequestMethod(requestMethod)
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
            }

            val scanner = new Scanner(conn.getInputStream, "UTF-8").useDelimiter("\\A")
            if(scanner.hasNext){
                return scanner.next
            }
        } catch {
            case e: IOException => {
                log.error("Exception while loading src from URL: {} [reason: {}]", url, e.getMessage)
                return null;
            }
            case e: SocketTimeoutException => {
                log.error("Timout while loading src from URL: {} [reason: {}]", url, e.getMessage)
                return null;
            }
        }
        return null;
    }

}
