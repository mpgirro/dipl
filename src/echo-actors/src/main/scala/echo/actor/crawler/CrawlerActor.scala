package echo.actor.crawler

import java.io.InputStreamReader
import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.0`
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream._
import akka.util.ByteString
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.core.domain.feed.FeedStatus
import echo.core.exception.EchoException
import echo.core.parse.api.FyydAPI
import org.apache.commons.io.IOUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpHead
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author Maximilian Irro
  */
class CrawlerActor extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WEBSITE_JOBS: Boolean = Option(CONFIG.getBoolean("echo.crawler.website-jobs")).getOrElse(false)

    // TODO define CONN_TIMEOUT
    // TODO define READ_TIMEOUT

    private final val DOWNLOAD_TIMEOUT_MS = 5 * 1000
    private final val DOWNLOAD_TIMEOUT = 10.seconds // TODO read from config
    private final val QUEUE_SIZE = 500 // TODO read from config file
    private final val DOWNLOAD_MAXBYTES = 5  * 1024 * 1024 // TODO load from config file

    // important, or we will experience starvation on processing many feeds at once
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

    private final implicit val actorSystem: ActorSystem = context.system
    private final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

    private implicit val sttpBackend = AkkaHttpBackend
            .usingActorSystem(actorSystem,
                              options = SttpBackendOptions.connectionTimeout(DOWNLOAD_TIMEOUT))

    private var parser: ActorRef = _
    private var directoryStore: ActorRef = _
    private var indexStore: ActorRef = _

    private val http = Http()
    private val fyydAPI: FyydAPI = new FyydAPI()

    private val requestQueueMap: mutable.Map[String, (SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])])] = mutable.Map.empty

    override def postStop: Unit = {
        Http().shutdownAllConnectionPools().onComplete(_ => log.info("shutting down"))
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
            /*
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
            */
            headSttp(echoId, url, job)

        case DownloadContent(echoId, url, job) =>
            log.debug("Received Download({},'{}',{})", echoId, url, job.getClass.getSimpleName)
            //download(echoId, url, job)
            downloadSttp(echoId, url, job)
            log.debug("Finished Download({},'{}',{})", echoId, url, job.getClass.getSimpleName)

        case CrawlFyyd(count) => onCrawlFyyd(count)

        case LoadFyydEpisodes(podcastId, fyydId) => onLoadFyydEpisodes(podcastId, fyydId)

        case PoisonPill =>
            log.debug("Received a PosionPill -> shutting down connection pool")
            http.shutdownAllConnectionPools()

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

    @Deprecated
    private def queueRequestAkka(url: String, request: HttpRequest): Future[HttpResponse] = {

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
                .throttle(1, 1.second, 1, ThrottleMode.shaping)
                //.idleTimeout(timeout) // TODO
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
        } (executionContext)
    }

    private def onCrawlFyyd(count: Int) = {
        log.debug("Received CrawlFyyd({})", count)
        val feeds = fyydAPI.getFeedUrls(count)
        log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)

        log.debug("Proposing these feeds to the internal directory now")
        val it = feeds.iterator()
        while(it.hasNext){
            directoryStore ! ProposeNewFeed(it.next())
        }
        log.debug("Finished CrawlFyyd({})", count)
    }

    private def onLoadFyydEpisodes(podcastId: String, fyydId: Long) = {
        log.debug("Received LoadFyydEpisodes({},'{}')", podcastId, fyydId)

        val json = fyydAPI.getEpisodesByPodcastIdJSON(fyydId)
        parser ! ParseFyydEpisodes(podcastId, json)

        log.debug("Finished LoadFyydEpisodes({},'{}')", podcastId, fyydId)
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

    @Deprecated
    private def evalAkkaResponse(url: String, response: HttpResponse): Try[(Option[String],Option[String],Option[String])] = {

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

    private def evalApacheResponse(url: String, response: org.apache.http.HttpResponse): Try[(Option[String],Option[String],Option[String])] = {
        // TODO
        val statusCode = response.getStatusLine.getStatusCode

        // we assume we will use the known URL to download later, but maybe this changes...
        var location: Option[String] = Some(url)
        statusCode match {
            case 200 => // all fine
            case 301 => // Moved Permanently
                // TODO do something with the new location, e.g. send message to directory to update episode, and use this to (re-)index the new website
                location = response
                    .getHeaders("location")
                    .map(_.getValue)
                    .headOption
                log.debug("Redirecting {} to {}", url, location.getOrElse("NON PROVIDED"))
            //log.warning("301 Moved Permanently reported, this is the new location : {} (of : {})", location.getOrElse("NON PROVIDED"), url)
            // TODO once I have a propper procedure for 301 handling, should I fail here?
            //return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.status.reason()}")) // TODO make a dedicated exception
            case 302 => // odd, but ok
            case 404 => // not found: nothing there worth processing
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.getStatusLine.getReasonPhrase}")) // TODO make a dedicated exception
            case 503 => // service unavailable
                return Failure(new EchoException(s"HEAD request reported status $statusCode : ${response.getStatusLine.getReasonPhrase}")) // TODO make a dedicated exception
            case _   =>
                log.warning("Received unexpected status from HEAD request : {} {} on {}", statusCode, response.getStatusLine.getReasonPhrase, url)
        }

        val mimeType: Option[String] = Option(response.getLastHeader("Content-Type"))
            .map(contentTypeHeader => Some(contentTypeHeader.getValue.split(";")(0).trim))
            .getOrElse(None)

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
        val eTag: Option[String] = response
            .getHeaders("etag")
            .map(_.getValue)
            .headOption

        //set the "last modified" header field if existent
        val lastModified: Option[String] = response
            .getHeaders("last-modified")
            .map(_.getValue)
            .headOption

        Success((location, eTag, lastModified))
    }

    /*
    @Deprecated
    private def testAndDownloadAkka(echoId: String, url: String, jobType: JobKind.Value): Unit = {
        val headRequest = HttpRequest(
            HttpMethods.HEAD,
            uri = url,
            protocol = `HTTP/1.0`)
        try {
            //val responseFuture: Future[HttpResponse] = Http(context.system).singleRequest(headRequest)
            queueRequestAkka(url, headRequest)
                .onComplete {

                    // TODO this match equals the code from the akka version 1:1
                    case Success(response) =>
                        try {
                            log.debug("Just got HEAD response from : {}", url)
                            evalAkkaResponse(url, response) match {
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
                                            sendErrorNotificationIfFeasable(echoId, url, jobType)
                                    }
                                case Failure(reason) =>
                                    log.warning("HEAD response prevented downloading resource : {}", Option(reason.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                                    sendErrorNotificationIfFeasable(echoId, url, jobType)
                            }
                        } finally {
                            //response.discardEntityBytes() // make sure the conncetion does not remain open longer than it must
                        }
                    case Failure(reason) =>
                        log.warning("HTTP HEAD request failed on resource : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(echoId, url, jobType)
                }
        } catch {
            case e: java.util.concurrent.TimeoutException =>
                log.error("Timeout on : {}", url)
            case e: Exception =>
                log.error("Exception while testing HEAD : {} [reason : {}]", url, e.getMessage)
                e.printStackTrace()
                sendErrorNotificationIfFeasable(echoId, url, jobType)
        } finally {
            //headRequest.discardEntityBytes()
        }
    }

    @Deprecated
    private def downloadAkka(echoId: String, url: String, jobType: JobKind.Value): Unit = {
        val getRequest = HttpRequest(
            HttpMethods.GET,
            uri = url,
            protocol = `HTTP/1.0`)
        try {
            //val responseFuture: Future[HttpResponse] = Http(context.system).singleRequest(getRequest)
            queueRequestAkka(url, getRequest)
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
                                log.error("Refusing to download resource because content length exceeds maximum: {} > {}", cl, DOWNLOAD_MAXBYTES)
                                throw new EchoException(s"Refusing to download resource because content length exceeds maximum: ${cl} > ${DOWNLOAD_MAXBYTES}")
                            }
                        })

                        //implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher") // TODO
                        log.debug("Collecting content toStrict for GET response : {}", url)
                        response.entity
                            .withSizeLimit(DOWNLOAD_MAXBYTES)
                            .toStrict(DOWNLOAD_TIMEOUT)
                            .map { _.data }
                            .map(_.utf8String) // get a real `String`
                            .onComplete {
                                case Success(data: String) =>
                                    log.debug("Finished content toStrict for GET response : {}", url)
                                    jobType match {
                                        case JobKind.FEED_NEW_PODCAST =>
                                            parser ! ParseNewPodcastData(url, echoId, data)
                                            directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                                        case JobKind.FEED_UPDATE_EPISODES =>
                                            parser ! ParseUpdateEpisodeData(url, echoId, data)
                                            directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
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
                                    sendErrorNotificationIfFeasable(echoId, url, jobType)
                            }

                    case Failure(reason) =>
                        log.warning("HTTP GET request failed on resource : '{}' [reason : {}]", url, reason.getMessage)
                        sendErrorNotificationIfFeasable(echoId, url, jobType)
                }
        } catch {
            case e: java.util.concurrent.TimeoutException =>
                log.error("Timeout on : {}", url)
            case e: Exception =>
                log.error("Exception while downloading resource : {} [reason : {}]", e.getMessage)
                e.printStackTrace()
                sendErrorNotificationIfFeasable(echoId, url, jobType)
        } finally {
            //getRequest.discardEntityBytes()
        }
    }
    */

    private def headCheck(echoId: String, url: String, job: FetchJob): Unit = {
        val requestConfig = RequestConfig.custom.
            setSocketTimeout(DOWNLOAD_TIMEOUT_MS)
            .setConnectTimeout(DOWNLOAD_TIMEOUT_MS)
            .build
        val httpclient: CloseableHttpClient = HttpClientBuilder
            .create
            .setDefaultRequestConfig(requestConfig)
            //.setConnectionManager(poolingHttpClientConnectionManager) // TODO
            .build
        try {
            var request = new org.apache.http.client.methods.HttpHead(url)
            val response: org.apache.http.client.methods.CloseableHttpResponse = httpclient.execute(request)

            log.debug("Just got HEAD response from : {}", url)
            evalApacheResponse(url, response) match {
                case Success((location, etag, lastMod)) =>
                    location match {
                        case Some(href) =>
                            log.debug("Sending message to download content : {}", href)
                            job match {
                                case WebsiteFetchJob() =>
                                    // if the link in the feed is redirected (which is often the case due
                                    // to some feed analytic tools, we set our records to the new location
                                    if(!url.equals(href)) {
                                        directoryStore ! UpdateLinkByEchoId(echoId, href)
                                        indexStore ! IndexStoreUpdateDocLink(echoId, href)
                                    }

                                    // we always download websites, because we only do it once anyway
                                    self ! DownloadContent(echoId, href, job)
                                case _ =>
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
                                    self ! DownloadContent(echoId, href, job)
                            }
                        case None =>
                            log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                            sendErrorNotificationIfFeasable(echoId, url, job)
                    }
                case Failure(reason) =>
                    log.warning("HEAD response prevented downloading resource : {} [reason : {}]", url, Option(reason.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                    sendErrorNotificationIfFeasable(echoId, url, job)
            }

            Success(response)
        } catch {
            case e: java.net.ConnectException =>
                log.error("Connection error on : {} [reason : {}]", e.getMessage, url)
                Failure(e)
            case e: java.net.UnknownHostException =>
                log.error("Unknown host : {}", url)
                Failure(e)
            case e: java.io.IOException =>
                log.error("IO error on : {} [reason : {}]", url, e.getMessage)
                Failure(e)
            case e: Exception =>
                log.error("Error : {}", e.getMessage)
                e.printStackTrace()
                Failure(e)
        } finally {
            httpclient.close() // TODO
        }
    }

    private def download(echoId: String, url: String, job: FetchJob): Unit = {
        val requestConfig = RequestConfig.custom.
            setSocketTimeout(DOWNLOAD_TIMEOUT_MS)
            .setConnectTimeout(DOWNLOAD_TIMEOUT_MS)
            .build
        val httpclient: CloseableHttpClient = HttpClientBuilder
            .create
            .setDefaultRequestConfig(requestConfig)
            //.setConnectionManager(poolingHttpClientConnectionManager) // TODO
            .build
        try {
            val request = new org.apache.http.client.methods.HttpGet(url)
            val response: org.apache.http.client.methods.CloseableHttpResponse = httpclient.execute(request)

            log.debug("Just got GET response from : {}", url)

            // once again, ensure we do not accidentally fetch a media file and take it for text
            Option(response.getEntity.getContentType).foreach(ct => {
                val mimeType = response.getEntity.getContentType.getValue.split(";")(0).trim
                if(!validMimeType(mimeType)){
                    log.error("Aborted before downloading a file with invalid MIME-type : '{}' from : '{}'", mimeType, url)
                    throw new EchoException(s"Aborted before downloading a file with invalid MIME-type : '${mimeType}'") // TODO make dedicated exception
                }
            })

            /* TODO
            // ensure we do not accidentally fetch a neverending stream
            if(response.entity.isIndefiniteLength()){
                log.error("Refusing to download resource with indefinite length from : '{}'", url)
                throw new EchoException("Refusing to download resource with indefinite length") // TODO make dedicated exception
            }
            */

            val cl = response.getEntity.getContentLength
            if(cl > DOWNLOAD_MAXBYTES) {
                log.error("Refusing to download resource because content length exceeds maximum: {} > {}", cl, DOWNLOAD_MAXBYTES)
                throw new EchoException(s"Refusing to download resource because content length exceeds maximum: ${cl} > ${DOWNLOAD_MAXBYTES}")
            }

            log.debug("Collecting content from GET response : {}", url)
            def extractData(r: org.apache.http.HttpResponse): Try[String] = {
                try {
                    val inputStream = response.getEntity.getContent
                    val data: String = IOUtils.toString(new InputStreamReader(inputStream))
                    inputStream.close()
                    Success(data)
                } catch {
                    case e: Exception =>
                        log.error("Error : {}", e.getMessage)
                        Failure(e)
                }
            }
            extractData(response) match {
                case Success(data) =>
                    log.debug("Finished collecting content from GET response : {}", url)
                    job match {
                        case NewPodcastFetchJob() =>
                            parser ! ParseNewPodcastData(url, echoId, data)
                            directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                        case UpdateEpisodesFetchJob(etag, lastMod) =>
                            parser ! ParseUpdateEpisodeData(url, echoId, data)
                            directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                        case WebsiteFetchJob() =>
                            parser ! ParseWebsiteData(echoId, data)
                    }
                case Failure(reason) =>
                    sendErrorNotificationIfFeasable(echoId, url, job)
            }
        } catch {
            case e: java.net.ConnectException =>
                log.error("Connection error on : {} [reason : {}]", e.getMessage, url)
                sendErrorNotificationIfFeasable(echoId, url, job)
            case e: java.net.UnknownHostException =>
                log.error("Unknown host : {}", url)
                sendErrorNotificationIfFeasable(echoId, url, job)
            case e: java.io.IOException =>
                log.error("IO error on : {} [reason : {}]", url, e.getMessage)
                sendErrorNotificationIfFeasable(echoId, url, job)
            case e: Exception =>
                log.error("Exception while downloading resource : {} [reason : {}]", e.getMessage)
                e.printStackTrace()
                sendErrorNotificationIfFeasable(echoId, url, job)
        } finally {
            httpclient.close()
        }
    }

    private def sendErrorNotificationIfFeasable(echoId: String, url: String, job: FetchJob): Unit = {
        job match {
            case WebsiteFetchJob() => // do nothing...
            case _ =>
                directoryStore ! FeedStatusUpdate(echoId, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
        }
    }

    private def headSttp(exo: String, url: String, job: FetchJob): Unit = {
        sttp
            .head(uri"${url}")
            .readTimeout(DOWNLOAD_TIMEOUT)
            .response(asString)
            .send()
            .onComplete {
                case Success(response) =>
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
                            if (!validMimeType(mime)) {
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

                        //set the "last modified" header field if existent
                        val lastModified: Option[String] = response.header("last-modified")

                        location match {
                            case Some(href) =>
                                log.debug("Sending message to download content : {}", href)
                                job match {
                                    case WebsiteFetchJob() =>
                                        // if the link in the feed is redirected (which is often the case due
                                        // to some feed analytic tools, we set our records to the new location
                                        if(!url.equals(href)) {
                                            directoryStore ! UpdateLinkByEchoId(exo, href)
                                            indexStore ! IndexStoreUpdateDocLink(exo, href)
                                        }

                                        // we always download websites, because we only do it once anyway
                                        self ! DownloadContent(exo, href, job)
                                    case _ =>
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
                                        self ! DownloadContent(exo, href, job)
                                }
                            case None =>
                                log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
                                sendErrorNotificationIfFeasable(exo, url, job)
                        }
                    }

                case Failure(reason) =>
                    // TODO
                    log.warning("HEAD response prevented downloading resource : {} [reason : {}]", url, Option(reason.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
                    sendErrorNotificationIfFeasable(exo, url, job)
            }


        // TODO
    }

    /**
      *
      * Docs for STTP: http://sttp.readthedocs.io/en/latest/
      *
      * @param exo
      * @param url
      * @param job
      */
    private def downloadSttp(exo: String, url: String, job: FetchJob): Unit = {
        //val response: Future[Response[Source[ByteString, Any]]] = sttp.get(...
        sttp
            .get(uri"${url}")
            .readTimeout(DOWNLOAD_TIMEOUT)
            //.response(asStream[Source[ByteString, Any]])
            .response(asString)
            .send()
            .onComplete {
                case Success(response) =>

                    if (!response.isSuccess) {
                        log.error("Download resulted in a non-success response code : {}", response.code)
                        throw new EchoException(s"Download resulted in a non-success response code : ${response.code}") // TODO make dedicated exception
                    }

                    response.contentType.foreach(ct => {
                        val mimeType = ct.split(";")(0).trim
                        if (!validMimeType(mimeType)) {
                            log.error("Aborted before downloading a file with invalid MIME-type : '{}' from : '{}'", mimeType, url)
                            throw new EchoException(s"Aborted before downloading a file with invalid MIME-type : '${mimeType}'") // TODO make dedicated exception
                        }
                    })

                    response.contentLength.foreach(cl => {
                        if (cl > DOWNLOAD_MAXBYTES) {
                            log.error("Refusing to download resource because content length exceeds maximum: {} > {}", cl, DOWNLOAD_MAXBYTES)
                            throw new EchoException(s"Refusing to download resource because content length exceeds maximum: ${cl} > ${DOWNLOAD_MAXBYTES}")
                        }
                    })

                    // TODO
                    response.body match {
                        case Left(errorMessage) =>
                            log.error("Error collecting download body, message : {}", errorMessage)
                            throw new EchoException(s"Error collecting download body, message : ${errorMessage}") // TODO make dedicated exception
                        case Right(deserializedBody) =>
                            val data = deserializedBody
                            log.debug("Finished collecting content from GET response : {}", url)
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
                    }

                case Failure(reason) =>
                    // TODO
                    log.error("Error downloading resource : {} [reason : {}]", url, reason.getMessage)
                    //throw new EchoException(s"Error downloading resource : ${url} [reason : ${reason.getMessage}]") // TODO make dedicated exception
                    sendErrorNotificationIfFeasable(exo, url, job)
            }
    }

}
