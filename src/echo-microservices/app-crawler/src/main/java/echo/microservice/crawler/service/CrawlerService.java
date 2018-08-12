package echo.microservice.crawler.service;

import echo.core.async.parser.ImmutableNewFeedParserJob;
import echo.core.async.parser.ImmutableUpdateFeedParserJob;
import echo.core.async.parser.ParserJob;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.exception.EchoException;
import echo.core.http.HeadResult;
import echo.core.http.HttpClient;
import echo.microservice.crawler.async.ParserQueueSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class CrawlerService {

    private final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private HttpClient httpClient;

    @Value("${echo.crawler.download-timeout:10}")
    private Long DOWNLOAD_TIMEOUT;

    @Value("${echo.crawler.download-maxbytes:5242880}")
    private Long DOWNLOAD_MAXBYTES;

    @Autowired
    private ParserQueueSender parserQueueSender;

    @PostConstruct
    private void init() {
        httpClient = new HttpClient(DOWNLOAD_TIMEOUT, DOWNLOAD_MAXBYTES);
    }

    @PreDestroy
    private void destroy() {
        log.info("Destructor called");
        Optional.ofNullable(httpClient).ifPresent(HttpClient::close);
    }

    @Async
    public void downloadFeed(String podcastExo, String feedUrl, boolean isNewPodcast, RoundTripTime rtt) {
        try {
            log.info("Performing HEAD check on : {}", feedUrl);
            final HeadResult headResult = httpClient.headCheck(feedUrl);
            if (headResult.getLocation().isPresent() ) {
                final String location = headResult.getLocation().get();

                if (!feedUrl.equals(location)) {
                    // TODO send update to catalog with new location
                }

                log.info("Fetching content from : {}", feedUrl);
                final String feedData = httpClient.fetchContent(feedUrl, headResult.getContentEncoding());

                final ParserJob job;
                if (isNewPodcast) {
                    job = ImmutableNewFeedParserJob.of(podcastExo, feedUrl, feedData, rtt.bumpRTTs());
                } else {
                    job = ImmutableUpdateFeedParserJob.of(podcastExo, feedUrl, feedData, rtt.bumpRTTs());
                }
                parserQueueSender.produceMsg(job);

            } else {
                log.error("We did not get any location-url after evaluating response --> cannot proceed download without one");
                //sendErrorNotificationIfFeasable(exo, url, job) TODO
            }
        } catch (EchoException e) {
            log.error("HEAD response prevented fetching resource : {} [reason : {}]", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
            // TODO send status update
        } catch (ConnectException e) {
            log.error("java.net.ConnectException for HEAD check on : {} [msg : {}]", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
        } catch (SocketTimeoutException e) {
            log.error("java.net.SocketTimeoutException for HEAD check on : {} [msg : {}]", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
        } catch (UnknownHostException e) {
            log.error("java.net.UnknownHostException for HEAD check on : {} [msg : {}]", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
        } catch (SSLHandshakeException e) {
            log.error("javax.net.ssl.SSLHandshakeException for HEAD check on : {} [msg : {}]", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
        } catch (Exception e) {
            log.error("Unhandled Exception on {} : {}", feedUrl, Optional.ofNullable(e.getMessage()).orElse("NO REASON GIVEN IN EXCEPTION"));
            e.printStackTrace();
        }

        /*
        // TODO
        log.debug("Retrieving feed-data as String : {}", feedUrl);
        final String feedData = restTemplate.getForObject(feedUrl, String.class);
        */

        // TODO
        // assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

    }

    @Async
    public void downloadWebsite(String podcastExo, String url) {
        // TODO
    }
}
