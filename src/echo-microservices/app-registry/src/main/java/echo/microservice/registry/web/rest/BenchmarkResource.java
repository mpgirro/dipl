package echo.microservice.registry.web.rest;

import com.google.common.collect.ImmutableList;
import echo.core.benchmark.*;
import echo.core.benchmark.mps.MessagesPerSecondMonitor;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.benchmark.rtt.RoundTripTimeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.List;

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

    private final RestTemplate restTemplate = new RestTemplate();

    private final BenchmarkUtil benchmarkUtil = new BenchmarkUtil("../../benchmark/");

    @RequestMapping(
        value  = "/rtt-report",
        method = RequestMethod.POST)
    public ResponseEntity<Void> rttReport(@RequestBody RoundTripTime rtt) throws URISyntaxException {
        log.debug("REST request to add RTT report : {}", rtt);
        rttMonitor.addRoundTripTime(rtt);
        if (rttMonitor.isFinished()) {
            sendStopMessagePerSecondMonitoringMessages();

            /*
            //rttMonitor.getAllRTTs().stream().forEach(r -> log.info(r.getRtts().toString()));
            log.info("RTT reporting finished; results in CSV format :");
            System.out.println(rttMonitor.toCsv());
            rttMonitor.printSumEvals();
            */

            final int size = rttMonitor.getAllRTTs().size();

            String progressFile = "msa-rtt-progress-not-set.txt";
            String overallFile = "msa-rtt-overall-not-set.txt";
            if (Workflow.PODCAST_INDEX == rttMonitor.getWorkflow() || Workflow.EPISODE_INDEX == rttMonitor.getWorkflow() ) {
                progressFile = "msa-index"+size+"-rtt-progress";
                overallFile  = "msa-index-rtt-overall";
            } else if (Workflow.RESULT_RETRIEVAL == rttMonitor.getWorkflow()) {
                progressFile = "msa-search"+size+"-rtt-progress";
                overallFile  = "msa-search-rtt-overall";
            } else {
                log.warn("Unhandled Workflow : {}", rttMonitor.getWorkflow());
            }

            //log.info("RTT progress CSV :");
            final String progressCSV = rttMonitor.getProgressCSV();
            benchmarkUtil.writeToFile(progressFile, progressCSV);

            //log.info("RTT overall CSV :");
            final String overallCSV = rttMonitor.getOverallCSV();
            benchmarkUtil.appendToFile(overallFile, overallCSV);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/mps-report",
        method = RequestMethod.POST,
        params = { "name", "mps" })
    public ResponseEntity<Void> mpsReport(@RequestParam("name") String name, @RequestParam("mps") Double mps) throws URISyntaxException {
        log.debug("REST request to report MPS : {} for unit : {}", mps, name);
        mpsMonitor.addMetric(name, mps);
        if (mpsMonitor.isFinished()) {
            log.info("MPS reporting finished; results in CSV format :");
            System.out.println(mpsMonitor.toCsv());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/benchmark-report",
        method = RequestMethod.POST)
    public ResponseEntity<Void> benchmarkReport(@RequestBody BenchmarkMeterReport report) throws URISyntaxException {
        log.debug("REST request to report benchmark results : {}", report);
        mpsMonitor.addMetric(report.getName(), report.getMps().mps);
        System.out.println(report.getName() + "\t: " + report.getMemoryUsage().meanBytesStr);
        System.out.println(report.getName() + "\t: " + report.getCpuLoad().meanLoadStr);
        if (mpsMonitor.isFinished()) {
            log.info("MPS reporting finished; results in CSV format :");
            System.out.println(mpsMonitor.toCsv());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/monitor-feed-progress",
        method = RequestMethod.POST)
    public ResponseEntity<Void> monitorFeedProgress(@RequestBody List<FeedProperty> feedProperties) throws URISyntaxException {
        log.info("REST request to monitor fed progress for properties list : {}", feedProperties);
        rttMonitor.initWithProperties(ImmutableList.copyOf(feedProperties));
        sendStartMessagePerSecondMonitoringMessages(true);
        mpsMonitor.reset();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/monitor-query-progress",
        method = RequestMethod.POST)
    public ResponseEntity<Void> monitorQueryProgress(@RequestBody List<String> queries) throws URISyntaxException {
        log.info("REST request to monitor fed progres for queries list : {}", queries);
        rttMonitor.initWithQueries(ImmutableList.copyOf(queries));
        sendStartMessagePerSecondMonitoringMessages(true);
        mpsMonitor.reset();
        return new ResponseEntity<>(HttpStatus.OK);
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
        log.info("Informing all MS to start monitoring MPS");
        startGatewayMps(mps);
        startCatalogMps(mps);
        startIndexMps(mps);
        startCrawlerMps(mps);
        startParserMps(mps);
        startSearcherMps(mps);
        startUpdaterMps(mps);
    }

    private void sendStopMessagePerSecondMonitoringMessages() {
        log.info("Informing all MS to stop monitoring MPS and report results");
        final boolean mps = false;
        //HttpEntity<Foo> request = new HttpEntity<>(new Foo("bar"));
        stopGatewayMps(mps);
        stopCatalogMps(mps);
        stopIndexMps(mps);
        stopCrawlerMps(mps);
        stopParserMps(mps);
        stopSearcherMps(mps);
        stopUpdaterMps(mps);
    }

    @Async
    public void startGatewayMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3030/benchmark/start-mps?mps="+mps, null, Void.class); // Gateway
    }

    @Async
    public void startCatalogMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3031/benchmark/start-mps?mps="+mps, null, Void.class); // Catalog
    }

    @Async
    public void startIndexMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3032/benchmark/start-mps?mps="+mps, null, Void.class); // Index
    }

    @Async
    public void startCrawlerMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3033/benchmark/start-mps?mps="+mps, null, Void.class); // Crawler
    }

    @Async
    public void startParserMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3034/benchmark/start-mps?mps="+mps, null, Void.class); // Parser
    }

    @Async
    public void startSearcherMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3035/benchmark/start-mps?mps="+mps, null, Void.class); // Searcher
    }

    @Async
    public void startUpdaterMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3037/benchmark/start-mps?mps="+mps, null, Void.class); // Updater
    }

    @Async
    public void stopGatewayMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3030/benchmark/stop-mps?mps="+mps, null, Void.class); // Gateway
    }

    @Async
    public void stopCatalogMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3031/benchmark/stop-mps?mps="+mps, null, Void.class); // Catalog
    }

    @Async
    public void stopIndexMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3032/benchmark/stop-mps?mps="+mps, null, Void.class); // Index
    }

    @Async
    public void stopCrawlerMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3033/benchmark/stop-mps?mps="+mps, null, Void.class); // Crawler
    }

    @Async
    public void stopParserMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3034/benchmark/stop-mps?mps="+mps, null, Void.class); // Parser
    }

    @Async
    public void stopSearcherMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3035/benchmark/stop-mps?mps="+mps, null, Void.class); // Searcher
    }

    @Async
    public void stopUpdaterMps(boolean mps) {
        restTemplate.postForObject("http://localhost:3037/benchmark/stop-mps?mps="+mps, null, Void.class); // Updater
    }

}
