package echo.microservice.catalog.web.rest;

import echo.core.domain.dto.*;
import echo.microservice.catalog.service.EpisodeService;
import echo.microservice.catalog.service.FeedService;
import echo.microservice.catalog.service.PodcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/catalog")
public class PodcastResource {

    private final Logger log = LoggerFactory.getLogger(PodcastResource.class);

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private FeedService feedService;

    @RequestMapping(
        value    = "/podcast",
        method   = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PodcastDTO> createPodcast(@RequestBody PodcastDTO podcast) throws URISyntaxException {
        log.debug("REST request to save Podcast : {}", podcast);
        final Optional<PodcastDTO> created = podcastService.save(podcast);
        return created
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @RequestMapping(
        value    = "/podcast",
        method   = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<PodcastDTO> updatePodcast(@RequestBody PodcastDTO podcast) {
        log.debug("REST request to update Podcast : {}", podcast);
        final Optional<PodcastDTO> updated = podcastService.update(podcast);
        return updated
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/podcast/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<PodcastDTO> getPodcast(@PathVariable String exo) {
        log.info("REST request to get Podcast (EXO) : {}", exo);
        final Optional<PodcastDTO> podcast = podcastService.findOneByExo(exo);
        return podcast
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/podcast/{exo}/episodes",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ArrayWrapperDTO> getEpisodesByPodcast(@PathVariable String exo) {
        log.info("REST request to get Episodes by Podcast (EXO) : {}", exo);
        final List<EpisodeDTO> episodes = episodeService.findAllByPodcast(exo);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(episodes),
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/podcast/{exo}/feeds",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ArrayWrapperDTO> getFeedsByPodcast(@PathVariable String exo) {
        log.info("REST request to get Feeds by Podcast (EXO) : {}", exo);
        final List<FeedDTO> feeds = feedService.findAllByPodcast(exo);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(feeds),
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/podcast",
        method   = RequestMethod.GET,
        params   = { "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ArrayWrapperDTO> getAll(@RequestParam("page") Integer page,
                                                  @RequestParam("size") Integer size) {
        log.info("REST request to get all Podcasts by page/size : ({},{})", page, size);
        final List<PodcastDTO> podcasts = podcastService.findAll(page, size);
        //final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/catalog/podcasts"); // TODO
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(podcasts),
            headers,
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/podcast/teaser",
        method   = RequestMethod.GET,
        params   = { "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ArrayWrapperDTO> getAllAsTeasers(@RequestParam("page") Integer page,
                                                           @RequestParam("size") Integer size) {
        log.info("REST request to get all Podcasts as teaser by page/size : ({},{})", page, size);
        final List<PodcastDTO> teasers = podcastService.findAllAsTeaser(page, size);
        //final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/catalog/podcasts/teasers"); // TODO
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(teasers),
            headers,
            HttpStatus.OK);
    }

}
