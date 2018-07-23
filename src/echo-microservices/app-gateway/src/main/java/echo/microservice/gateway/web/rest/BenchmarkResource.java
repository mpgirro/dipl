package echo.microservice.gateway.web.rest;

import echo.core.benchmark.MessagesPerSecondCounter;
import echo.core.benchmark.RoundTripTime;
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

    @Resource(name = "messagesPerSecondCounter")
    private MessagesPerSecondCounter mpsCounter;

    @RequestMapping(
        value  = "/start-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to start MPS counting");
        mpsCounter.startCounting();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to stop MPS counting");
        mpsCounter.stopCounting();
        benchmarkClient.setMpsReport(applicationName, mpsCounter.getMessagesPerSecond());
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value    = "/get-mps",
        method   = RequestMethod.GET)
    public Double getMpsValue() {
        log.debug("REST request to get MPS");
        mpsCounter.incrementCounter();
        return mpsCounter.getMessagesPerSecond();
    }

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
