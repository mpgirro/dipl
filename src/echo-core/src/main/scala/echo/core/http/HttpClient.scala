package echo.core.http

import java.nio.file.Paths
import java.util.Optional

import com.softwaremill.sttp._
import com.typesafe.scalalogging.Logger
import echo.core.exception.EchoException

import scala.compat.java8.OptionConverters._
import scala.concurrent.duration._
import scala.io.Source

/**
  *
  * @param timeout timeout in seconds
  * @param DOWNLOAD_MAXBYTES
  * @author Maximilian Irro
  */
class HttpClient (val timeout: Long,
                  val DOWNLOAD_MAXBYTES: Long) {

    private val log = Logger(classOf[HttpClient])

    private val DOWNLOAD_TIMEOUT = timeout.seconds

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

        // we will ensure that the protocol will be lower case, otherwise further stuff will fail
        val parts = url.split("://")
        if (parts.size != 2) {
            throw new IllegalArgumentException("No valid URL provided : " + url)
        }
        val cleanUrl = parts(0).toLowerCase + "://" + parts(1)

        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            headCheckHTTP(cleanUrl)
        } else if (cleanUrl.startsWith("file:///")) {
            headCheckFILE(cleanUrl)
        } else {
            throw new IllegalArgumentException("URL points neither to local nor remote resource : " + url)
        }
    }

    private def headCheckHTTP(url: String): HeadResult = {
        val response = emptyRequest // use empty request, because standard req uses header "Accept-Encoding: gzip" which can cause problems with HEAD requests
            .head(uri"${url}")
            .readTimeout(DOWNLOAD_TIMEOUT)
            .acceptEncoding("")
            .send()

        // we assume we will use the known URL to download later, but maybe this changes...
        var location: Option[String] = Some(url)

        if (!response.isSuccess) {
            response.code match {
                case 200 => // all fine
                case 301 => // Moved Permanently
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

        // extract the character encoding of the resource if it is returned, to avoid encoding problems
        val encoding: Option[String] = response.contentType
            .map(ct => ct.split(";")
                .lift(1)                      // -> charset="UTF-8"
                .map(_.split("=")
                .lift(1)                  // -> "UTF-8"
                .map( _
                .replaceAll("\"", "") // remove quotation marks if any
                .trim ))              // remove whitespace
                .getOrElse(None))
            .getOrElse(None)

        //set the etag if existent
        val eTag: Option[String] = response.header("etag")

        //set the "last modified" header field if existent
        val lastModified: Option[String] = response.header("last-modified")

        new HeadResult(
            response.code,
            location.asJava,
            mimeType.asJava,
            encoding.asJava,
            eTag.asJava,
            lastModified.asJava
        )
    }

    private def headCheckFILE(url: String): HeadResult = {

        val path = Paths.get(url)
        val mimeType = java.nio.file.Files.probeContentType(path)
        val file = path.toFile
        val status = if (file.exists()) 200 else 404

        new HeadResult(
            status,
            Optional.of(url),
            Optional.of(Option(mimeType).getOrElse("text/xml")),
            Optional.of("UTF-8"),
            Optional.empty(),
            Optional.empty()
        )
    }

    @throws(classOf[EchoException])
    @throws(classOf[java.net.ConnectException])
    @throws(classOf[java.net.SocketTimeoutException])
    @throws(classOf[java.net.UnknownHostException])
    @throws(classOf[javax.net.ssl.SSLHandshakeException])
    def fetchContent(url: String, encoding: java.util.Optional[String]): String = {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            fetchContentHTTP(url, encoding)
        } else if (url.startsWith("file:///")) {
            fetchContentFILE(url, encoding)
        } else {
            throw new IllegalArgumentException("URL points neither to local nor remote resource : " + url)
        }
    }

    private def fetchContentHTTP(url: String, encoding: java.util.Optional[String]): String = {
        val request = sttp
            .get(uri"${url}")
            .readTimeout(DOWNLOAD_TIMEOUT)
            .response(asByteArray) // prevent assuming UTF-8 encoding, because some feeds do not use it

        encoding.asScala.foreach(e => request.acceptEncoding(e))

        val response = request.send()

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
            case Right(data) =>
                log.debug("Finished collecting content from GET response : {}", url)

                new String(data, encoding.asScala.getOrElse("utf-8"))
        }
    }

    private def fetchContentFILE(url: String, encoding: java.util.Optional[String]): String = {
        Source.fromURL(url).getLines.mkString
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
