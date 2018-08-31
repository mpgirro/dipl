package echo.microservice.gateway.web.rest;

import echo.core.benchmark.BenchmarkSearchRequest;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.service.BenchmarkService;
import echo.microservice.gateway.service.SearcherService;
import echo.microservice.gateway.web.client.BenchmarkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
    private BenchmarkClient benchmarkClient;

    @Autowired
    private SearcherService searcherService;

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
    public ResponseEntity<ResultWrapperDTO> benchmarkSearchRequest(@RequestBody BenchmarkSearchRequest request) {
        log.info("REST request to search for request : {}", request);
        mpsMeter.tick();
        final Optional<ResultWrapperDTO> resultWrapper = searcherService.searchBenchmark(request.getQuery(), request.getPage(), request.getSize(), request.getRtt());

        if (resultWrapper.isPresent()) {
            sendRttReport(resultWrapper.get().getRTT().bumpRTTs());
        } else {
            log.warn("No result present");
            sendRttReport(request.getRtt().bumpRTTs());
        }

        return resultWrapper
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Async
    public void sendRttReport(RoundTripTime rtt) {
        benchmarkClient.sendRttReport(rtt);
    }

}
