package echo.microservice.catalog.web.rest;

import echo.core.domain.dto.PodcastDTO;
import echo.microservice.catalog.service.PodcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@RestController
@RequestMapping("/api/directory")
public class PodcastResource {

    private final Logger log = LoggerFactory.getLogger(PodcastResource.class);

    @Autowired
    private PodcastService podcastService;

    @RequestMapping(value = "/podcast/{echoId}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
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
