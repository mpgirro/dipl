package echo.microservice.index.controller;

import echo.core.domain.dto.PodcastDTO;
import echo.microservice.index.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api/index")
public class SearchController {

    private final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "/search/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PodcastDTO> searchQuery(@PathVariable String query) {
        log.debug("REST request to search index for query : {}", query);
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
