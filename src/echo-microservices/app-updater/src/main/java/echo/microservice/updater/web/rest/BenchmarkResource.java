package echo.microservice.updater.web.rest;

import echo.core.benchmark.CpuLoadMeter;
import echo.core.benchmark.MemoryUsageMeter;
import echo.core.benchmark.MessagesPerSecondMeter;
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

    @Value("${spring.application.name:echo-updater}")
    private String applicationName;

    @Autowired
    private BenchmarkClient benchmarkClient;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @Resource(name = "memoryUsageMeter")
    private MemoryUsageMeter memoryUsageMeter;

    @Resource(name = "cpuLoadMeter")
    private CpuLoadMeter cpuLoadMeter;

    @RequestMapping(
        value  = "/start-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to start MPS counting");
        mpsMeter.startMeasurement();
        memoryUsageMeter.startMeasurement();
        cpuLoadMeter.startMeasurement();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to stop MPS counting");
        mpsMeter.stopMeasurement();
        memoryUsageMeter.stopMeasurement();
        cpuLoadMeter.stopMeasurement();
        benchmarkClient.setMpsReport(applicationName, mpsMeter.getMessagesPerSecond());
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/get-mps",
        method   = RequestMethod.GET)
    public Double getMpsValue() {
        log.debug("REST request to get MPS");
        mpsMeter.incrementCounter();
        return mpsMeter.getMessagesPerSecond();
    }

}
