package echo.actor.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.0`
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, QueueOfferResult}
import echo.actor.ActorProtocol._
import echo.core.exception.EchoException
import echo.core.model.feed.FeedStatus
import echo.core.parse.api.FyydAPI

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author Maximilian Irro
  */
class CrawlerActor extends Actor with ActorLogging {

    private final val DOWNLOAD_TIMEOUT = 3 // TODO read from config
    private final val QUEUE_SIZE = 1500 // TODO read from config file
    private final val DOWNLOAD_MAXBYTES = 5  * 1024 * 1024 // TODO load from config file

    private val downloadTimeout = 60.seconds // TODO read value from config

    // important, or we will experience starvation on processing many feeds at once
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

    private final implicit val system: ActorSystem = context.system
    private final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _
    private var indexStore: ActorRef = _

    private val http = Http()
    private val fyydAPI: FyydAPI = new FyydAPI()

    private val requestQueueMap: mutable.Map[String, (SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])])] = mutable.Map.empty

    override def postStop: Unit = {
        http.shutdownAllConnectionPools().onComplete(_ => log.info(s"${self.path.name} shut down"))
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

    private def queueRequest(url: String, request: HttpRequest): Future[HttpResponse] = {

        // TODO remove elements from the queueMap after a certain to too prevent bloating

        val (host,protocol) = analyzeUrl(url)

        if(!requestQueueMap.contains(host)){ // TODO ||  requestQueueMap(host)==null --> can be freed after the idle timeout!

            log.debug("Creating Pool for : {} (due to {})", host, url)

            val pool = if (protocol.equals("https")) {
                http.cachedHostConnectionPoolHttps[Promise[HttpResponse]](host)
            } else {
                http.cachedHostConnectionPool[Promise[HttpResponse]](host)
            }

            val queue = Source
                .queue[(HttpRequest, Promise[HttpResponse])](QUEUE_SIZE, OverflowStrategy.backpressure) // TODO try OverflowStrategy.backpressure
                .via(pool)
                .toMat(Sink.foreach({
                    case ((Success(resp), p)) => p.success(resp)
                    case ((Failure(e), p))    => p.failure(e)
                }))(Keep.left)
                .run
            requestQueueMap += (host -> queue)
        }
        val queue = requestQueueMap(host)
        val responsePromise = Promise[HttpResponse]()
        queue.offer(request -> responsePromise).flatMap {
            case QueueOfferResult.Enqueued    => responsePromise.future
            case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
            case QueueOfferResult.Failure(ex) => Future.failed(ex)
            case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
        }
    }

    private def analyzeUrl(url: String): (String, String) = {

        // http, https, ftp if provided
        val protocol = if(url.indexOf("://") > -1) {
            url.split("://")(0)
        } else {
            ""
        }

        val hostname = {
            if (url.indexOf("://") > -1)
                url.split('/')(2)
            else
                url.split('/')(0)
        }  .split(':')(0) // find & remove port number
            .split('?')(0) // find & remove "?"
           // .split('/')(0) // find & remove the "/" that might still stick at the end of the hostname

        (hostname, protocol)
    }

    private def validMimeType(mime: String): Boolean = {
        mime match {
            case "application/rss+xml"  => true // feed
            case "application/xml"      => true // feed
            case "text/xml"             => true // feed
            case "text/html"            => true // website
            case "text/plain"           => true // might be ok and might be not -> will have to check manually
            case "none/none"            => true // might be ok and might be not -> will have to check manually
            case "application/octet-stream" => true // some sites use this, but might also be used for media files
            case _                      => false
        }
    }

    private def evalResponse(url: String, response: HttpResponse): Try[(Option[String],Option[String],Option[String])] = {

        val statusCode = response.status.intValue

        if(response.status.isRedirection){
            // TODO do something with the redirection, like updating the Feed entity
        }

        // we assume we will use the known URL to download later, but maybe this changes...
        var location: Option[String] = Some(url)

        statusCode match {
            case 200 => // all fine
            case 301 => // Moved Permanently
                // TODO do something with the new location, e.g. send message to directory to update episode, and use this to (re-)index the new website
                location = response.headers
                    .filter(_.is("location"))
                    .map(_.value)
                    .headOption
                log.debug("Redirecting {} to {}", url, location.getOrElse("NON PROVIDED"))
                //log.warning("301 Moved Permanently reported, this is the new location : {} (of : {})", location.getOrElse("NON PROVIDED"), url)
            // TODO once I have a propper procedure for 301 handling, should I fail here?
            //return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case 302 => // odd, but ok
            case 404 => // not found: nothing there worth processing
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case 503 => // service unavailable
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case _   =>
                log.warning("Received unexpected status from HEAD request : {} {} on {}", statusCode, response.status.reason(), url)
        }

        val mimeType: Option[String] = Option(response.entity.contentType.mediaType.value)
        mimeType match {
            case Some(mime) =>
                if(!validMimeType(mime)){
                    mime match {
                        case _@("audio/mpeg" | "application/octet-stream") =>
                            return Failure(new EchoException(s"Invalid MIME-type '$mime' of $url")) // TODO make a dedicated exception
                        case _ =>
                            //log.warning("Received unexpected MIME type '{}' from HEAD request to : '{}'", mime, url)
                            return Failure(new EchoException(s"Unexpected MIME type '$mime' of '$url'")) // TODO make a dedicated exception
                    }
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

        Success((location, eTag, lastModified))
    }

    private def testAndDownloadAsync(echoId: String, url: String, jobType: JobKind.Value): Unit = {
        val headRequest = HttpRequest(
            HttpMethods.HEAD,
            uri = url,
            protocol = `HTTP/1.0`)
        try {
            //val responseFuture: Future[HttpResponse] = Http(context.system).singleRequest(headRequest)
            queueRequest(url, headRequest)
                .onComplete {
                    case Success(response) =>
                        try {
                            log.debug("Just got HEAD response from : {}", url)
                            evalResponse(url, response) match {
                                case Success((location, etag, lastMod)) =>
                                    location match {
                                        case Some(href) =>
                                            log.debug("Sending message to download async : {}", href)
                                            jobType match {
                                                case JobKind.WEBSITE =>

                                                    // if the link in the feed is redirected (which is often the case due
                                                    // to some feed analytic tools, we set our records to the new location
                                                    if(!url.equals(href)) {
                                                        directoryStore ! UpdateLinkByEchoId(echoId, href)
                                                        indexStore ! IndexStoreUpdateDocLink(echoId, href)
                                                    }

                                                    // we always download websites, because we only do it once anyway
                                                    self ! DownloadAsync(echoId, href, jobType)
                                                case _ => // either FEED_NEW_PODCAST or FEED_UPDATE_EPISODES

                                                    // if the feed moved to a new URL, we will inform the directory, so
                                                    // it will use the new location starting with the next update cycle
                                                    if(!url.equals(href)) {
                                                        directoryStore ! UpdateFeedUrl(url, href)
                                                    }

                                                    /*
                                                     * TODO
                                                     * here I have to do some voodoo with etag/lastMod to
                                                     * determine weither the feed changed and I really need to redownload
                                                     */
                                                    self ! DownloadAsync(echoId, href, jobType)
                                            }
                                        case None =>
                                            log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                                            sendErrorNotificationIfFeasable(url, jobType)
                                    }
                                case Failure(reason) =>
                                    log.warning("HEAD response prevented downloading resource : {}", Option(reason.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                                    sendErrorNotificationIfFeasable(url, jobType)
                            }
                        } finally {
                            response.discardEntityBytes() // make sure the conncetion does not remain open longer than it must
                        }
                    case Failure(reason) =>
                        log.warning("HTTP HEAD request failed on resource : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(url, jobType)
                }
        } catch {
            case e: Exception =>
                log.error("Exception while testing HEAD : {} [reason : {}]", url, e.getMessage)
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
            //val responseFuture: Future[HttpResponse] = Http(context.system).singleRequest(getRequest)
            queueRequest(url, getRequest)
                .onComplete {
                    case Success(response) =>
                        log.info("Just got GET response from : {}", url)
                        // once again, ensure we do not accidentally fetch a media file and take it for text
                        if(!validMimeType(response.entity.contentType.mediaType.value)){
                            log.error("Aborted before downloading a file with invalid MIME-type : '{}' from : '{}'", response.entity.contentType.mediaType.value, url)
                            throw new EchoException(s"Aborted before downloading a file with invalid MIME-type : '${response.entity.contentType.mediaType.value}'") // TODO make dedicated exception
                        }

                        // ensure we do not accidentally fetch a neverending stream
                        if(response.entity.isIndefiniteLength()){
                            log.error("Refusing to download resource with indefinite length from : '{}'", url)
                            throw new EchoException("Refusing to download resource with indefinite length") // TODO make dedicated exception
                        }

                        response.entity.contentLengthOption.foreach(cl => {
                            if(cl > DOWNLOAD_MAXBYTES){
                                log.error("Refusing to download resource because content length exceeds maximum: {} > ", cl, DOWNLOAD_MAXBYTES)
                            }
                        })

                        //implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher") // TODO
                        log.debug("Collecting content toStrict for GET response : {}", url)
                        response.entity
                            .withSizeLimit(DOWNLOAD_MAXBYTES)
                            .toStrict(downloadTimeout)
                            .map { _.data }
                            .map(_.utf8String) // get a real `String`
                            .onComplete {
                                case Success(data: String) =>
                                    log.debug("Finished content toStrict for GET response : {}", url)
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
                                case Success(data) =>
                                    log.error("The Success(data) data has an unexpected type : {}", data.getClass)
                                case Failure(reason) =>
                                    reason match {
                                        case e: java.util.concurrent.TimeoutException =>
                                            log.warning("Failed to download resource due to timeout : {}", url)
                                        case e =>
                                            log.error("Failed to collect response body HTML into a String : {} [reason : {} from message type : {}]", url, e.getMessage, e.getClass)
                                            //e.printStackTrace()
                                    }
                                    sendErrorNotificationIfFeasable(url, jobType)
                            }

                    case Failure(reason) =>
                        log.warning("HTTP GET request failed on resource : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(url, jobType)
                }
        } catch {
            case e: Exception =>
                log.error("Exception while downloading resource : {} [reason : {}]", e.getMessage)
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
