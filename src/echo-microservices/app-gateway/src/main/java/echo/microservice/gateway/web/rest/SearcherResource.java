package echo.microservice.gateway.web.rest;

import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.service.SearcherService;
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

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api")
public class SearcherResource {

    private final Logger log = LoggerFactory.getLogger(SearcherResource.class);

    @Autowired
    private SearcherService searcherService;

    @RequestMapping(value = "/search",
        method = RequestMethod.GET,
        params = { "q", "p", "s" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("q") String query,
                                                        @RequestParam("p") Integer page,
                                                        @RequestParam("s") Integer size) {
        log.debug("REST request to search for q/p/s : ('{}',{},{})", query, page, size);
        final Optional<ResultWrapperDTO> resultWrapper = searcherService.search(query, page, size);
        return resultWrapper
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
