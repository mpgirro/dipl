package echo.microservice.parser.web.rest;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.microservice.parser.service.BenchmarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
