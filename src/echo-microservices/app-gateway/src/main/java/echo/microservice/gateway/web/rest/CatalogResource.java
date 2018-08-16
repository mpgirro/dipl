package echo.microservice.gateway.web.rest;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.domain.dto.*;
import echo.microservice.gateway.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api")
public class CatalogResource {

    private final Logger log = LoggerFactory.getLogger(CatalogResource.class);

    @Value("${echo.gateway.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.gateway.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private CatalogService catalogService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value    = "/podcast/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PodcastDTO> getPodcast(@PathVariable String exo) {
        log.info("REST request to get Podcast (EXO) : {}", exo);
        mpsMeter.tick();
        final Optional<PodcastDTO> podcast = catalogService.getPodcast(exo);
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
    public ResponseEntity<ArrayWrapperDTO> getEpisodesByPodcast(@PathVariable String exo) {
        log.info("REST request to get Episodes by Podcast (EXO) : {}", exo);
        mpsMeter.tick();
        final List<EpisodeDTO> episodes = catalogService.getEpisodesByPodcast(exo);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(episodes),
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/podcast/{exo}/feeds",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getFeedsByPodcast(@PathVariable String exo) {
        log.info("REST request to get Feeds by Podcast (EXO) : {}", exo);
        mpsMeter.tick();
        final List<FeedDTO> feeds = catalogService.getFeedsByPodcast(exo);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(feeds),
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/podcast",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getAllPodcasts(@RequestParam("p") Optional<Integer> page,
                                                          @RequestParam("s") Optional<Integer> size) {
        log.info("REST request to get all Podcasts by page/size : ({},{})", page, size);
        mpsMeter.tick();
        final List<PodcastDTO> podcasts = catalogService.getAllPodcasts(page, size);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(podcasts),
            HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/episode/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EpisodeDTO> getEpisode(@PathVariable String exo) {
        log.info("REST request to get Episode (EXO) : {}", exo);
        mpsMeter.tick();
        final Optional<EpisodeDTO> episode = catalogService.getEpisode(exo);
        return episode
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/episode/{exo}/chapters",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getChaptersByEpisode(@PathVariable String exo) {
        log.info("REST request to get Chapters by Episode (EXO) : {}", exo);
        mpsMeter.tick();
        final List<ChapterDTO> chapters = catalogService.getChaptersByEpisode(exo);
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(chapters),
            HttpStatus.OK);
    }

}
