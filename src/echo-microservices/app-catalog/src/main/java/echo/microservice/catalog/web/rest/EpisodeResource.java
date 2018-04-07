package echo.microservice.catalog.web.rest;

import echo.core.async.catalog.RegisterEpisodeIfNewJobCatalogJob;
import echo.core.domain.dto.ArrayWrapperDTO;
import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.ImmutableArrayWrapperDTO;
import echo.core.mapper.IdMapper;
import echo.microservice.catalog.service.ChapterService;
import echo.microservice.catalog.service.EpisodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/catalog")
public class EpisodeResource {

    private final Logger log = LoggerFactory.getLogger(EpisodeResource.class);

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private ChapterService chapterService;

    private IdMapper idMapper = IdMapper.INSTANCE;

    @PostMapping("/episode")
    @Transactional
    public ResponseEntity<EpisodeDTO> createEpisode(@RequestBody EpisodeDTO episode) throws URISyntaxException {
        log.debug("REST request to save Episode : {}", episode);
        final Optional<EpisodeDTO> created = episodeService.save(episode);
        return created
            .map(idMapper::clearImmutable)
            .map(e -> new ResponseEntity<>(
                (EpisodeDTO) e,
                HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PostMapping("/episode/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerEpisode(@RequestBody RegisterEpisodeIfNewJobCatalogJob job) throws URISyntaxException {
        log.debug("REST request to register episode by Podcast (EXO) : {}", job.getPodcastExo());
        episodeService.register(job);
    }

    @PutMapping("/episode")
    @Transactional
    public ResponseEntity<EpisodeDTO> updateEpisode(@RequestBody EpisodeDTO episode) {
        log.debug("REST request to update Episode : {}", episode);
        final Optional<EpisodeDTO> updated = episodeService.update(episode);
        return updated
            .map(idMapper::clearImmutable)
            .map(e -> new ResponseEntity<>(
                (EpisodeDTO) e,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/episode/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<EpisodeDTO> getEpisode(@PathVariable String exo) {
        log.debug("REST request to get Episode (EXO) : {}", exo);
        final Optional<EpisodeDTO> episode = episodeService.findOneByExo(exo);
        return episode
            .map(idMapper::clearImmutable)
            .map(e -> new ResponseEntity<>(
                (EpisodeDTO) e,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value    = "/episode/{exo}/chapters",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<ArrayWrapperDTO> getChaptersByEpisode(@PathVariable String exo) {
        log.debug("REST request to get Chapters by Episode (EXO) : {}", exo);
        final List<ChapterDTO> chapters = chapterService.findAllByEpisode(exo).stream()
            .map(idMapper::clearImmutable)
            .collect(Collectors.toList());
        return new ResponseEntity<>(
            ImmutableArrayWrapperDTO.of(chapters),
            HttpStatus.OK);
    }

}
