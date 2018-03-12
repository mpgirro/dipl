package echo.microservice.directory.web.rest;

import echo.core.domain.dto.EpisodeDTO;
import echo.microservice.directory.service.EpisodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class EpisodeResource {

    private final Logger log = LoggerFactory.getLogger(EpisodeResource.class);

    @Autowired
    private EpisodeService episodeService;

    @RequestMapping(value = "/episode/{echoId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<EpisodeDTO> getPodcast(@PathVariable String echoId) {
        log.debug("REST request to get Episode : {}", echoId);
        final Optional<EpisodeDTO> episode = episodeService.findOneByEchoId(echoId);
        return episode
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
