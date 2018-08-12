package echo.microservice.gateway.web.rest;

import echo.core.benchmark.cpu.CpuLoadMeter;
import echo.core.benchmark.memory.MemoryUsageMeter;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.service.SearcherService;
import echo.microservice.gateway.web.client.BenchmarkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.application.name:echo-gateway}")
    private String applicationName;

    @Autowired
    private BenchmarkClient benchmarkClient;

    @Autowired
    private SearcherService searcherService;

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
        return new ResponseEntity<>(HttpStatus.OK);
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
        benchmarkClient.mpsReport(applicationName, mpsMeter.getResult().mps);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/get-mps",
        method   = RequestMethod.GET)
    public Double getMpsValue() {
        log.debug("REST request to get MPS");
        mpsMeter.incrementCounter();
        return mpsMeter.getResult().mps;
    }

    @RequestMapping(
        value    = "/search",
        method   = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultWrapperDTO> searchQuery(@RequestParam("q") String query,
                                                        @RequestParam("p") Optional<Integer> page,
                                                        @RequestParam("s") Optional<Integer> size,
                                                        @RequestBody RoundTripTime rtt) {
        log.info("REST request to search for q/p/s : ('{}',{},{})", query, page, size);
        mpsMeter.incrementCounter();
        final Optional<ResultWrapperDTO> resultWrapper = searcherService.searchBenchmark(query, page, size, rtt);

        if (resultWrapper.isPresent()) {
            sendRttReport(resultWrapper.get().getRTT().bumpRTTs());
        } else {
            log.warn("No result present");
            sendRttReport(rtt.bumpRTTs());
        }

        return resultWrapper
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Async
    public void sendRttReport(RoundTripTime rtt) {
        benchmarkClient.rttReport(rtt);
    }

}
