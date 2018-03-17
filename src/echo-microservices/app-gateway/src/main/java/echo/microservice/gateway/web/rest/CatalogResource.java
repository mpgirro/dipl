package echo.microservice.gateway.web.rest;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.PodcastDTO;
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
    public ResponseEntity<List<EpisodeDTO>> getEpisodesByPodcast(@PathVariable String exo) {
        log.debug("REST request to get Episodes by Podcast (EXO) : {}", exo);
        final List<EpisodeDTO> episodes = catalogService.getEpisodesByPodcast(exo);
        return new ResponseEntity<>(episodes, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast/{exo}/feeds",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FeedDTO>> getFeedsByPodcast(@PathVariable String exo) {
        log.debug("REST request to get Feeds by Podcast (EXO) : {}", exo);
        final List<FeedDTO> feeds = catalogService.getFeedsByPodcast(exo);
        return new ResponseEntity<>(feeds, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast",
        method = RequestMethod.GET,
        params = { "p", "s" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PodcastDTO>> getAllPodcasts(@RequestParam("p") Integer page,
                                                           @RequestParam("s") Integer size) {
        log.debug("REST request to get all Podcasts by page/size : ({},{})", page, size);
        final List<PodcastDTO> podcasts = catalogService.getAllPodcasts(page, size);
        return new ResponseEntity<>(podcasts, HttpStatus.OK);
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
    public ResponseEntity<List<ChapterDTO>> getChaptersByEpisode(@PathVariable String exo) {
        log.debug("REST request to get Chapters by Episode (EXO) : {}", exo);
        final List<ChapterDTO> chapters = catalogService.getChaptersByEpisode(exo);
        return new ResponseEntity<>(chapters, HttpStatus.OK);
    }


}
