package echo.microservice.searcher.web.rest;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.searcher.service.BenchmarkService;
import echo.microservice.searcher.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.Optional;

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
    private SearchService searchService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value  = "/start-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startBenchmarkMeters(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.info("REST request to start benchmark meters");
        benchmarkService.startBenchmarkMeters();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopBenchmarkMeters(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.info("REST request to stop benchmark meters and report results");
        benchmarkService.stopBenchmarkMetersAndSendReport();
        return new ResponseEntity<>(HttpStatus.OK);
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
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("query") String query,
                                                        @RequestParam("page") Optional<Integer> page,
                                                        @RequestParam("size") Optional<Integer> size,
                                                        @RequestBody RoundTripTime rtt) {
        log.info("REST request to search for query/page/size : ('{}',{},{})", query, page, size);
        mpsMeter.tick();
        final ResultWrapperDTO result = searchService.searchBenchmark(query, page, size, rtt);
        final ResultWrapperDTO newRes = ImmutableResultWrapperDTO.builder()
            .from(result)
            .setRTT(result.getRTT().bumpRTTs())
            .create();
        return new ResponseEntity<>(
            newRes,
            HttpStatus.OK);
    }

}
