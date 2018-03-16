package echo.microservice.catalog.web.rest;

import echo.core.domain.dto.EpisodeDTO;
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
import java.util.Optional;

@RestController
@RequestMapping("/catalog")
public class EpisodeResource {

    private final Logger log = LoggerFactory.getLogger(EpisodeResource.class);

    @Autowired
    private EpisodeService episodeService;

    @PostMapping("/episode")
    @Transactional
    public ResponseEntity<EpisodeDTO> createEpisode(@RequestBody EpisodeDTO episodeDTO) throws URISyntaxException {
        log.debug("REST request to save Episode : {}", episodeDTO);
        final Optional<EpisodeDTO> created = episodeService.save(episodeDTO);
        return created
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @PutMapping("/episode")
    @Transactional
    public ResponseEntity<EpisodeDTO> updateEpisode(@RequestBody EpisodeDTO episodeDTO) {
        log.debug("REST request to update Episode : {}", episodeDTO);
        final Optional<EpisodeDTO> updated = episodeService.update(episodeDTO);
        return updated
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/episode/{exo}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<EpisodeDTO> getEpisode(@PathVariable String exo) {
        log.debug("REST request to get Episode (EXO) : {}", exo);
        final Optional<EpisodeDTO> episode = episodeService.findOneByEchoId(exo);
        return episode
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}