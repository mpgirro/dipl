package echo.microservice.registry.web.rest;

import com.google.common.collect.ImmutableList;
import echo.core.benchmark.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URISyntaxException;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/benchmark")
public class BenchmarkResource {

    private final Logger log = LoggerFactory.getLogger(BenchmarkResource.class);

    @Resource(name = "roundTripTimeMonitor")
    private RoundTripTimeMonitor rttMonitor;

    @Resource(name = "messagesPerSecondMonitor")
    private MessagesPerSecondMonitor mpsMonitor;

    final RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(
        value  = "/rtt-report",
        method = RequestMethod.POST)
    public ResponseEntity<Void> rttReport(@RequestBody RoundTripTime rtt) throws URISyntaxException {
        log.debug("REST request to add RTT report : {}", rtt);
        rttMonitor.addRoundTripTime(rtt);
        if (rttMonitor.isFinished()) {
            sendStopMessagePerSecondMonitoringMessages();
            rttMonitor.logResults();
        }
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/mps-report",
        method = RequestMethod.POST,
        params = { "name", "mps" })
    public ResponseEntity<Void> mpsReport(@RequestParam("name") String name, @RequestParam("mps") Double mps) throws URISyntaxException {
        log.debug("REST request to report MPS : {} for unit : {}", mps, name);
        mpsMonitor.addAndPrintMetric(name, mps);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }


    @RequestMapping(
        value  = "/monitor-feed-progress",
        method = RequestMethod.POST)
    public ResponseEntity<Void> monitorFeedProgress(@RequestBody ImmutableList<FeedProperty> feedProperties) throws URISyntaxException {
        log.debug("REST request to monitor fed progress for properties list");
        // TODO

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/monitor-query-progress",
        method = RequestMethod.POST)
    public ResponseEntity<Void> monitorQueryProgress(@RequestBody ImmutableList<String> queries) throws URISyntaxException {
        log.debug("REST request to monitor fed progres for queries list");
        // TODO

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/start-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> startMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to start MPS counting");
        sendStartMessagePerSecondMonitoringMessages(mps);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to stop MPS counting");
        sendStopMessagePerSecondMonitoringMessages();
        return new ResponseEntity<Void>(HttpStatus.OK);
    }


    private void sendStartMessagePerSecondMonitoringMessages(Boolean mps) {
        //HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        restTemplate.postForObject("http://localhost:3030/benchmark/start-mps?mps="+mps, null, Void.class); // Gateway
        restTemplate.postForObject("http://localhost:3031/benchmark/start-mps?mps="+mps, null, Void.class); // Catalog
        restTemplate.postForObject("http://localhost:3032/benchmark/start-mps?mps="+mps, null, Void.class); // Index
        restTemplate.postForObject("http://localhost:3033/benchmark/start-mps?mps="+mps, null, Void.class); // Crawler
        restTemplate.postForObject("http://localhost:3034/benchmark/start-mps?mps="+mps, null, Void.class); // Parser
        restTemplate.postForObject("http://localhost:3035/benchmark/start-mps?mps="+mps, null, Void.class); // Searcher
        restTemplate.postForObject("http://localhost:3037/benchmark/start-mps?mps="+mps, null, Void.class); // Updater
    }

    private void sendStopMessagePerSecondMonitoringMessages() {
        final boolean mps = false;
        //HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        restTemplate.postForObject("http://localhost:3030/benchmark/stop-mps?mps="+mps, null, Void.class); // Gateway
        restTemplate.postForObject("http://localhost:3031/benchmark/stop-mps?mps="+mps, null, Void.class); // Catalog
        restTemplate.postForObject("http://localhost:3032/benchmark/stop-mps?mps="+mps, null, Void.class); // Index
        restTemplate.postForObject("http://localhost:3033/benchmark/stop-mps?mps="+mps, null, Void.class); // Crawler
        restTemplate.postForObject("http://localhost:3034/benchmark/stop-mps?mps="+mps, null, Void.class); // Parser
        restTemplate.postForObject("http://localhost:3035/benchmark/stop-mps?mps="+mps, null, Void.class); // Searcher
        restTemplate.postForObject("http://localhost:3037/benchmark/stop-mps?mps="+mps, null, Void.class); // Updater

    }

}
