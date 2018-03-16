package echo.microservice.crawler.service;

import echo.core.async.job.ParserJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Maximilian Irro
 */
@Service
public class CrawlerService {

    private final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private RestTemplate restTemplate = new RestTemplate();

    public void downloadFeed(String podcastExo, String feedUrl) {
        // TODO
        log.debug("Retrieving feed-data as String : {}", feedUrl);
        final String feedData = restTemplate.getForObject(feedUrl, String.class);

        // TODO replace by sending job to queue
        final ParserJob job = new ParserJob();
        job.setExo(podcastExo);
        job.setUrl(feedUrl);
        job.setData(feedData);

        final String parserUrl = "http://localhost:3034/parser/new-podcast";
        log.debug("Sending feed-data to parser with request : {}", parserUrl);
        final HttpEntity<ParserJob> request = new HttpEntity<>(job);
        final ResponseEntity<Void> response = restTemplate.exchange(parserUrl, HttpMethod.POST, request, Void.class);

        // TODO
        // assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

    }


    public void downloadWebsite(String podcastExo, String url) {
        // TODO
    }
}
