package echo.microservice.catalog.web.rest;

import echo.core.domain.dto.FeedDTO;
import echo.microservice.catalog.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@RequestMapping("/catalog")
public class FeedResource {

    private final Logger log = LoggerFactory.getLogger(FeedResource.class);

    @Autowired
    private FeedService feedService;

    @PostMapping("/feed")
    @Transactional
    public ResponseEntity<FeedDTO> createFeed(@RequestBody FeedDTO feedDTO) throws URISyntaxException {
        log.debug("REST request to save Feed : {}", feedDTO);
        final Optional<FeedDTO> created = feedService.save(feedDTO);
        return created
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @RequestMapping(value = "/feed/propose",
        method = RequestMethod.POST,
        params = { "url" })
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public void proposeFeed(@RequestParam("url") String url) throws URISyntaxException {
        log.debug("REST request to propose Feed by URL : {}", url);
        feedService.propose(url);
    }

    @PutMapping("/feed")
    @Transactional
    public ResponseEntity<FeedDTO> updateFeed(@RequestBody FeedDTO feedDTO) {
        log.debug("REST request to update Feed : {}", feedDTO);
        final Optional<FeedDTO> updated = feedService.update(feedDTO);
        return updated
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/feed/{exo}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<FeedDTO> getFeed(@PathVariable String exo) {
        log.debug("REST request to get Feed (EXO) : {}", exo);
        final Optional<FeedDTO> feed = feedService.findOneByEchoId(exo);
        return feed
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
