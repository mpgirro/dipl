package echo.microservice.updater.web.rest;

import echo.core.benchmark.cpu.CpuLoadMeter;
import echo.core.benchmark.memory.MemoryUsageMeter;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.microservice.updater.service.BenchmarkService;
import echo.microservice.updater.web.client.BenchmarkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value  = "/start-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to start benchmark meters");
        benchmarkService.startBenchmarkMeters();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-benchmark-meters",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to stop benchmark meters");
        benchmarkService.stopBenchmarkMetersAndSendReport();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/get-mps",
        method   = RequestMethod.GET)
    public Double getMpsValue() {
        log.debug("REST request to get MPS");
        mpsMeter.incrementCounter();
        return mpsMeter.getResult().mps;
    }

}
