package echo.microservice.gateway.web.rest;

import echo.core.domain.dto.*;
import echo.microservice.gateway.web.dto.ArrayWrapperDTO;
import echo.microservice.gateway.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api")
public class CatalogResource {

    private final Logger log = LoggerFactory.getLogger(CatalogResource.class);

    @Autowired
    private CatalogService catalogService;

    @RequestMapping(value = "/podcast/{exo}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PodcastDTO> getPodcast(@PathVariable String exo) {
        log.debug("REST request to get Podcast (EXO) : {}", exo);
        final Optional<PodcastDTO> podcast = catalogService.getPodcast(exo);
        return podcast
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/podcast/{exo}/episodes",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getEpisodesByPodcast(@PathVariable String exo) {
        log.debug("REST request to get Episodes by Podcast (EXO) : {}", exo);
        final List<EpisodeDTO> episodes = catalogService.getEpisodesByPodcast(exo);
        final ArrayWrapperDTO<EpisodeDTO> result = new ArrayWrapperDTO<>(episodes);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast/{exo}/feeds",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getFeedsByPodcast(@PathVariable String exo) {
        log.debug("REST request to get Feeds by Podcast (EXO) : {}", exo);
        final List<FeedDTO> feeds = catalogService.getFeedsByPodcast(exo);
        final ArrayWrapperDTO<FeedDTO> result = new ArrayWrapperDTO<>(feeds);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast",
        method = RequestMethod.GET,
        params = { "p", "s" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getAllPodcasts(@RequestParam("p") Integer page,
                                                           @RequestParam("s") Integer size) {
        log.debug("REST request to get all Podcasts by page/size : ({},{})", page, size);
        final List<PodcastDTO> podcasts = catalogService.getAllPodcasts(page, size);
        final ArrayWrapperDTO<PodcastDTO> result = new ArrayWrapperDTO<>(podcasts);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/episode/{exo}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EpisodeDTO> getEpisode(@PathVariable String exo) {
        log.debug("REST request to get Episode (EXO) : {}", exo);
        final Optional<EpisodeDTO> episode = catalogService.getEpisode(exo);
        return episode
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/episode/{exo}/chapters",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayWrapperDTO> getChaptersByEpisode(@PathVariable String exo) {
        log.debug("REST request to get Chapters by Episode (EXO) : {}", exo);
        final List<ChapterDTO> chapters = catalogService.getChaptersByEpisode(exo);
        final ArrayWrapperDTO<ChapterDTO> result = new ArrayWrapperDTO<>(chapters);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
