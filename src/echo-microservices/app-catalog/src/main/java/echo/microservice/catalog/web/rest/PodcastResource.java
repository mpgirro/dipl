package echo.microservice.catalog.web.rest;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.microservice.catalog.service.EpisodeService;
import echo.microservice.catalog.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/catalog")
public class PodcastResource {

    private final Logger log = LoggerFactory.getLogger(PodcastResource.class);

    @Value("${echo.catalog.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.catalog.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private EpisodeService episodeService;

    @RequestMapping(value = "/podcast/{exo}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<PodcastDTO> getPodcast(@PathVariable String exo) {
        log.debug("REST request to get Podcast (EXO) : {}", exo);
        final Optional<PodcastDTO> podcast = podcastService.findOneByEchoId(exo);
        return podcast
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/podcast/{exo}/episodes",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<EpisodeDTO>> getEpisodesByPodcast(@PathVariable String exo) {
        log.debug("REST request to get Podcast (EXO) : {}", exo);
        final List<EpisodeDTO> episodes = episodeService.findAllByPodcast(exo);
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(episodes, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast",
        method = RequestMethod.GET,
        params = { "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<PodcastDTO>> getAll(@RequestParam("page") Integer page,
                                                   @RequestParam("size") Integer size) {
        log.debug("REST request to get all Podcasts");

        final int p = MoreObjects.firstNonNull(page, DEFAULT_PAGE) - 1;
        final int s = MoreObjects.firstNonNull(size, DEFAULT_SIZE);

        final List<PodcastDTO> podcasts = podcastService.findAll(p, s);
        //final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/catalog/podcasts"); // TODO
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(podcasts, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/podcast/teaser",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<List<PodcastDTO>> getAllAsTeasers() {
        log.debug("REST request to get all Podcasts as teaser");
        final List<PodcastDTO> teasers = podcastService.findAllAsTeaser();
        //final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/catalog/podcasts/teasers"); // TODO
        final HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(teasers, headers, HttpStatus.OK);
    }

}
