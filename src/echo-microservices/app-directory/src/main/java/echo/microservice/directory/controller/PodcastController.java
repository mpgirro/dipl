package echo.microservice.directory.controller;

import echo.core.domain.dto.PodcastDTO;
import echo.microservice.directory.service.PodcastService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PodcastController {

    private final Logger log = LoggerFactory.getLogger(PodcastController.class);

    @Inject
    private PodcastService podcastService;

    @RequestMapping(value = "/podcast/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PodcastDTO> getPodcast(@PathVariable String echoId) {
        log.debug("REST request to get Podcast : {}", echoId);
        final Optional<PodcastDTO> podcast = podcastService.findOneByEchoId(echoId);
        return podcast
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
