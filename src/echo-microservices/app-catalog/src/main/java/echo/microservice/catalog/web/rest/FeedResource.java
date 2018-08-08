package echo.microservice.catalog.web.rest;

import echo.core.benchmark.MessagesPerSecondMeter;
import echo.core.benchmark.RoundTripTime;
import echo.core.domain.dto.FeedDTO;
import echo.core.mapper.IdMapper;
import echo.microservice.catalog.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/catalog")
public class FeedResource {

    private final Logger log = LoggerFactory.getLogger(FeedResource.class);

    @Autowired
    private FeedService feedService;

    private IdMapper idMapper = IdMapper.INSTANCE;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @PostMapping("/feed")
    @Transactional
    public ResponseEntity<FeedDTO> createFeed(@RequestBody FeedDTO feed) throws URISyntaxException {
        log.debug("REST request to save Feed : {}", feed);
        mpsMeter.incrementCounter();
        final Optional<FeedDTO> created = feedService.save(feed);
        return created
            .map(idMapper::clearImmutable)
            .map(f -> new ResponseEntity<>(
                (FeedDTO) f,
                HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @RequestMapping(
        value  = "/feed/propose",
        method = RequestMethod.POST,
        params = { "url" })
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public void proposeFeed(@RequestParam("url") String url) throws URISyntaxException {
        log.debug("REST request to propose Feed by URL : {}", url);
        mpsMeter.incrementCounter();
        feedService.propose(url, RoundTripTime.empty());
    }

    @PutMapping("/feed")
    @Transactional
    public ResponseEntity<FeedDTO> updateFeed(@RequestBody FeedDTO feed) {
        log.debug("REST request to update Feed : {}", feed);
        mpsMeter.incrementCounter();
        final Optional<FeedDTO> updated = feedService.update(feed);
        return updated
            .map(idMapper::clearImmutable)
            .map(f -> new ResponseEntity<>(
                (FeedDTO) f,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/feed/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<FeedDTO> getFeed(@PathVariable String exo) {
        log.debug("REST request to get Feed (EXO) : {}", exo);
        mpsMeter.incrementCounter();
        final Optional<FeedDTO> feed = feedService.findOneByExo(exo);
        return feed
            .map(idMapper::clearImmutable)
            .map(f -> new ResponseEntity<>(
                (FeedDTO) f,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
