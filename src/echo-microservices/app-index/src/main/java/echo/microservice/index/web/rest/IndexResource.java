package echo.microservice.index.web.rest;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import echo.microservice.index.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URISyntaxException;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/index")
public class IndexResource {

    private final Logger log = LoggerFactory.getLogger(IndexResource.class);

    @Autowired
    private IndexService indexService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value    = "/search",
        method   = RequestMethod.GET,
        params   = { "query", "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("query") String query,
                                                        @RequestParam("page") Integer page,
                                                        @RequestParam("size") Integer size) throws SearchException {
        log.info("REST request to search index for query/page/size : ('{}',{},{})", query, page, size);
        mpsMeter.tick();
        final ResultWrapperDTO result = indexService.search(query, page, size, RoundTripTime.empty());
        return new ResponseEntity<>(
            result,
            HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/doc",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void addDoc(@RequestBody IndexDocDTO doc) throws URISyntaxException {
        log.debug("REST request to save doc : {}", doc.getExo());
        mpsMeter.tick();
        indexService.add(doc);
    }

    @RequestMapping(
        value    = "/doc/{exo}",
        method   = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<IndexDocDTO> getDoc(@PathVariable String exo) {
        log.debug("REST request to get index document: {}", exo);
        mpsMeter.tick();
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
