package echo.core.http

import com.softwaremill.sttp._
import com.typesafe.scalalogging.Logger
import echo.core.exception.EchoException

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.compat.java8.OptionConverters._

/**
  * @author Maximilian Irro
  */
class HttpClient (val DOWNLOAD_TIMEOUT: FiniteDuration,
                  val DOWNLOAD_MAXBYTES: Long) {

    private val log = Logger(classOf[HttpClient])

    private implicit val sttpBackend = HttpURLConnectionBackend(options = SttpBackendOptions.connectionTimeout(DOWNLOAD_TIMEOUT))

    def close(): Unit = {
        sttpBackend.close()
    }

    @throws(classOf[EchoException])
    @throws(classOf[java.net.ConnectException])
    @throws(classOf[java.net.SocketTimeoutException])
    @throws(classOf[java.net.UnknownHostException])
    @throws(classOf[javax.net.ssl.SSLHandshakeException])
    def headCheck(url: String): HeadResult = {

        val response = emptyRequest // use empty request, because standard req uses header "Accept-Encoding: gzip" which can cause problems with HEAD requests
            .response(ignore)
            .readTimeout(DOWNLOAD_TIMEOUT)
            .head(uri"${url}")
            .send()

        // we assume we will use the known URL to download later, but maybe this changes...
        var location: Option[String] = Some(url)

        if (!response.isSuccess) {
            response.code match {
                case 200 => // all fine
                case 301 => // Moved Permanently
                    // TODO do something with the new location, e.g. send message to directory to update episode, and use this to (re-)index the new website
                    location = response.header("location")
                    log.debug("Redirecting {} to {}", url, location.getOrElse("NON PROVIDED"))
                case 302 => // odd, but ok
                case 404 => // not found: nothing there worth processing
                    throw new EchoException(s"HEAD request reported status ${response.code} : ${response.statusText}")
                case 503 => // service unavailable
                    throw new EchoException(s"HEAD request reported status ${response.code} : ${response.statusText}")
                case _   =>
                    log.warn("Received unexpected status from HEAD request : {} {} on {}", response.code, response.statusText, url)
            }
        }

        val mimeType: Option[String] = response.contentType
            .map(ct => Some(ct.split(";")(0).trim))
            .getOrElse(None)

        mimeType match {
            case Some(mime) =>
                if (!isValidMime(mime)) {
                    mime match {
                        case _@("audio/mpeg" | "application/octet-stream") =>
                            throw new EchoException(s"Invalid MIME-type '${mime}' of '${url}'")
                        case _ =>
                            throw new EchoException(s"Unexpected MIME-type '${mime}' of '${url}")
                    }
                }
            case None =>
                // got no content type from HEAD request, therefore I'll just have to download the whole thing and look for myself
                log.warn("Did not get a Content-Type from HEAD request : {}", url)
        }

        //set the etag if existent
        val eTag: Option[String] = response.header("etag")

        //set the "last modified" header field if existent
        val lastModified: Option[String] = response.header("last-modified")

        val result: HeadResult = new HeadResult
        result.setStatusCode(response.code)
        result.setMimeType(mimeType.asJava)
        result.setLocation(location.asJava)
        result.seteTag(eTag.asJava)
        result.setLastModified(lastModified.asJava)

        result
    }

    @throws(classOf[EchoException])
    @throws(classOf[java.net.ConnectException])
    @throws(classOf[java.net.SocketTimeoutException])
    @throws(classOf[java.net.UnknownHostException])
    @throws(classOf[javax.net.ssl.SSLHandshakeException])
    def fetchContent(url: String): String = {

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
                deserializedBody // this will be returned
        }
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

}
