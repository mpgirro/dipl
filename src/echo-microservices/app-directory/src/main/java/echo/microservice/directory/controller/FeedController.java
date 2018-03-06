package echo.microservice.directory.controller;

import echo.core.domain.dto.FeedDTO;
import echo.microservice.directory.service.FeedService;
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
public class FeedController {

    private final Logger log = LoggerFactory.getLogger(FeedController.class);

    @Autowired
    private FeedService feedService;

    @RequestMapping(value = "/feed/{echoId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<FeedDTO> getPodcast(@PathVariable String echoId) {
        log.debug("REST request to get Feed : {}", echoId);
        final Optional<FeedDTO> feed = feedService.findOneByEchoId(echoId);
        return feed
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
