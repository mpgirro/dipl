package echo.actor.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.0`
import akka.http.scaladsl.model.headers.`Set-Cookie`
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import echo.actor.ActorProtocol._
import echo.core.exception.EchoException
import echo.core.model.feed.FeedStatus
import echo.core.parse.api.FyydAPI

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author Maximilian Irro
  */
object AkkaHttpCrawlerActor {

}


class AkkaHttpCrawlerActor extends Actor with ActorLogging {

    private final val DOWNLOAD_TIMEOUT = 3 // TODO read from config

    import context.dispatcher

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _

    private val fyydAPI: FyydAPI = new FyydAPI()

    private val internalTimeout = 5.seconds // TODO read value from config

    private final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

    private val http = Http(context.system)

    // TODO this is where I cache the request for a given host, so I do not process more than one conncetion at a time
    //private val hostRequestCache: mutable.Map[String, (String, String)] = mutable.Map[String, (String,String)].empty

    override def receive: Receive = {

        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directoryStore = ref

        case FetchFeedForNewPodcast(url, podcastId) =>
            log.info("Received FetchFeedForNewPodcast('{}', {})", url, podcastId)
            testAndDownloadAsync(podcastId, url, JobKind.FEED_NEW_PODCAST)

        case FetchFeedForUpdateEpisodes(url, podcastId) =>
            log.info("Received FetchFeedForUpdateEpisodes('{}',{})", url, podcastId)
            testAndDownloadAsync(podcastId, url, JobKind.FEED_UPDATE_EPISODES)

        case FetchWebsite(echoId, url) =>
            log.debug("Received FetchWebsite({},'{}')", echoId, url)
            testAndDownloadAsync(echoId, url, JobKind.WEBSITE)

        case DownloadAsync(echoId, url, jobType) =>
            log.debug("Received DownloadAsync({},'{}',{})", echoId, url, jobType)
            downloadAsync(echoId, url, jobType)

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

    private def evalResponse(response: HttpResponse): Try[(Option[String],Option[String])] = {

        val statusCode = response.status.intValue

        if(response.status.isRedirection){
            // TODO do something with the redirection, like updating the Feed entity
        }

        statusCode match {
            case 200 => // all fine
            case 301 => // Moved Permanently
                // TODO do something with the new location, e.g. send message to directory to update episode, and use this to (re-)index the new website
                val location: Option[String] = response.headers
                    .filter(_.is("location"))
                    .map(_.value)
                    .headOption
                log.warning("301 Moved Permanently reported, this is the new location : {}", location.getOrElse("NON PROVIDED"))
                // TODO once I have a propper procedure for 301 handling, should I fail here?
                //return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case 404 => // not found: nothing there worth processing
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case 503 => // service unavailable
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case _   =>
                log.warning("Received unexpected status from HEAD request : {} {}", statusCode, response.status.reason())
        }

        val mimeType: Option[String] = Option(response.entity.contentType.mediaType.value)
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
                        log.warning("Received unexpected MIME type from HEAD request : '{}'", mime)
                        return Failure(new EchoException(s"We do not process '$mime' (yet?)")) // TODO make a dedicated exception
                }
            case None =>
                // got no content type from HEAD request, therefore I'll just have to download the whole thing and look for myself
                log.warning("Did not get a Content-Type from HEAD request")
        }

        //set the etag if existent
        val eTag: Option[String] = response.headers
            .filter(_.is("etag"))
            .map(_.value)
            .headOption

        //set the "last modified" header field if existent
        val lastModified: Option[String] = response.headers
            .filter(_.is("last-modified"))
            .map(_.value)
            .headOption

        Success((eTag, lastModified))
    }

    private def testAndDownloadAsync(echoId: String, url: String, jobType: JobKind.Value): Unit = {
        val headRequest = HttpRequest(
            HttpMethods.HEAD,
            uri = url,
            protocol = `HTTP/1.0`)
        try {
            val responseFuture: Future[HttpResponse] = http.singleRequest(headRequest)
            responseFuture
                .onComplete {
                    case Success(response) =>
                        try {
                            val testResult = evalResponse(response)
                            testResult match {
                                case Success((etag, lastMod)) =>

                                    jobType match {
                                        case JobKind.WEBSITE =>
                                            // we always download websites, because we only do it once anyway
                                            self ! DownloadAsync(echoId, url, jobType)
                                        case _ => // either FEED_NEW_PODCAST or FEED_UPDATE_EPISODES
                                            /*
                                             * TODO
                                             * here I have to do some voodoo with etag/lastMod to
                                             * determine weither the feed changed and I really need to redownload
                                             */
                                            self ! DownloadAsync(echoId, url, jobType)
                                    }
                                case Failure(reason) =>
                                    log.error("HEAD request evaluation prevented downloading resource : {} [reason : {}]", url, Option(reason.getMessage).getOrElse("NON GIVEN IN EXCEPTION"))
                                    sendErrorNotificationIfFeasable(url, jobType)
                            }
                        } finally {
                            response.discardEntityBytes()
                        }
                    case Failure(reason) =>
                        log.warning("HTTP HEAD request failed on resource : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(url, jobType)
                }
        } catch {
            case e: Exception =>
                log.error(e.getMessage)
                e.printStackTrace()
                sendErrorNotificationIfFeasable(url, jobType)
        } finally {
            headRequest.discardEntityBytes()
        }
    }

    private def downloadAsync(echoId: String, url: String, jobType: JobKind.Value): Unit = {
        val getRequest = HttpRequest(
            HttpMethods.GET,
            uri = url,
            protocol = `HTTP/1.0`)
        try {
            val responseFuture: Future[HttpResponse] = http.singleRequest(getRequest)
            responseFuture
                .onComplete {
                    case Success(response) =>
                        try {
                            val htmlFuture: Future[String] = response.entity
                                .toStrict(internalTimeout)
                                .map { _.data }
                                .map(_.utf8String) // get a real `String`
                            htmlFuture
                                .onComplete {
                                    case Success(data) =>
                                        jobType match {
                                            case JobKind.FEED_NEW_PODCAST =>
                                                parser ! ParseNewPodcastData(url, echoId, data)
                                                directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                                            case JobKind.FEED_UPDATE_EPISODES =>
                                                parser ! ParseEpisodeData(url, echoId, data)
                                                directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                                            case JobKind.WEBSITE =>
                                                parser ! ParseWebsiteData(echoId, data)
                                        }
                                    case Failure(reason) =>
                                        log.error("Failed to collect response body HTML into a String : {} [reason : {}]", url, reason.getMessage)
                                        sendErrorNotificationIfFeasable(url, jobType)
                                }
                        } finally {
                            response.discardEntityBytes()
                        }
                    case Failure(reason) =>
                        log.warning("HTTP HEAD request failed on website : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(url, jobType)
                }
        } catch {
            case e: Exception =>
                log.error(e.getMessage)
                e.printStackTrace()
                sendErrorNotificationIfFeasable(url, jobType)
        } finally {
            getRequest.discardEntityBytes()
        }
    }

    private def sendErrorNotificationIfFeasable(url: String, jobType: JobKind.Value): Unit = {
        jobType match {
            case JobKind.WEBSITE => // do nothing...
            case _ =>
                directoryStore ! FeedStatusUpdate(url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
        }
    }

}
