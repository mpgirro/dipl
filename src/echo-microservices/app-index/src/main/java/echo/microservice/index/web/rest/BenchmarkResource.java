package echo.microservice.index.web.rest;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.exception.SearchException;
import echo.microservice.index.service.BenchmarkService;
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
@RequestMapping("/benchmark")
public class BenchmarkResource {

    private final Logger log = LoggerFactory.getLogger(BenchmarkResource.class);

    @Autowired
    private BenchmarkService benchmarkService;

    @Autowired
    private IndexService indexService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value  = "/start-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startBenchmarkMeters(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.info("REST request to start benchmark meters");
        benchmarkService.startBenchmarkMeters();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopBenchmarkMeters(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.info("REST request to stop benchmark meters and report results");
        benchmarkService.stopBenchmarkMetersAndSendReport();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/get-mps",
        method   = RequestMethod.GET)
    public Double getMpsValue() {
        log.debug("REST request to get MPS");
        mpsMeter.tick();
        return mpsMeter.getResult().getMps();
    }

    @RequestMapping(
        value    = "/search",
        method   = RequestMethod.POST,
        params   = { "query", "page", "size" },
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("query") String query,
                                                        @RequestParam("page") Integer page,
                                                        @RequestParam("size") Integer size,
                                                        @RequestBody RoundTripTime rtt) throws SearchException {
        log.info("REST request to search index for query/page/size : ('{}',{},{})", query, page, size);
        mpsMeter.tick();
        final ResultWrapperDTO result = indexService.search(query, page, size, rtt);
        return new ResponseEntity<>(
            result,
            HttpStatus.OK);
    }

}
