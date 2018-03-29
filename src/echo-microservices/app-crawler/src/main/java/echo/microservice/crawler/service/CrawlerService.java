package echo.microservice.crawler.service;

import echo.core.async.job.ParserJob;
import echo.core.exception.EchoException;
import echo.core.http.HeadResult;
import echo.core.http.HttpClient;
import echo.microservice.crawler.async.ParserQueueSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final String PARSER_URL = "http://localhost:3034"; // TODO

    private final RestTemplate restTemplate = new RestTemplate();

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
    public void downloadFeed(String podcastExo, String feedUrl) {
        try {
            final HeadResult headResult = httpClient.headCheck(feedUrl);
            if (headResult.getLocation().isPresent() ) {
                final String location = headResult.getLocation().get();

                if (!feedUrl.equals(location)) {
                    // TODO send update to catalog with new location
                }

                final String feedData = httpClient.fetchContent(feedUrl, headResult.getContentEncoding());

                // TODO replace by sending job to queue
                final ParserJob job = new ParserJob();
                job.setExo(podcastExo);
                job.setUrl(feedUrl);
                job.setData(feedData);

                // TODO
                //parserQueueSender.produceMsg("<Parse-New-Feed : " + feedUrl + ">");
                parserQueueSender.produceMsg(job);

                /*
                final String parserUrl = PARSER_URL+"/parser/new-podcast";
                log.debug("Sending feed-data to parser with request : {}", parserUrl);
                final HttpEntity<ParserJob> request = new HttpEntity<>(job);
                final ResponseEntity<Void> response = restTemplate.exchange(parserUrl, HttpMethod.POST, request, Void.class);
                */

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
