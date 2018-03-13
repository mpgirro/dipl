package echo.microservice.searcher.web.rest;

import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.searcher.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api/searcher")
public class SearchResource {

    private final Logger log = LoggerFactory.getLogger(SearchResource.class);

    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "/search",
        method = RequestMethod.GET,
        params = { "query", "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("query") String query,
                                                        @RequestParam("page") Integer page,
                                                        @RequestParam("size") Integer size) {
        log.debug("REST request to search for query/page/size : ('{}',{},{})", query, page, size);
        final ResultWrapperDTO result = searchService.search(query, page, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
