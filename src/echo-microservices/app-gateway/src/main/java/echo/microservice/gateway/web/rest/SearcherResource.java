package echo.microservice.gateway.web.rest;

import echo.core.benchmark.MessagesPerSecondCounter;
import echo.core.benchmark.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.service.SearcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource(name = "messagesPerSecondCounter")
    private MessagesPerSecondCounter mpsCounter;

    @RequestMapping(
        value    = "/search",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("q") String query,
                                                        @RequestParam("p") Optional<Integer> page,
                                                        @RequestParam("s") Optional<Integer> size,
                                                        @RequestBody RoundTripTime rtt) {
        log.info("REST request to search for q/p/s : ('{}',{},{})", query, page, size);
        mpsCounter.incrementCounter();
        final Optional<ResultWrapperDTO> resultWrapper = searcherService.search(query, page, size, rtt);
        return resultWrapper
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
