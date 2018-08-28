package echo.microservice.registry.web.rest;

import com.google.common.collect.ImmutableList;
import echo.core.benchmark.*;
import echo.core.benchmark.cpu.CpuLoadResult;
import echo.core.benchmark.memory.MemoryUsageResult;
import echo.core.benchmark.mps.MessagesPerSecondMonitor;
import echo.core.benchmark.mps.MessagesPerSecondResult;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.benchmark.rtt.RoundTripTimeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final String GATEWAY_URL  = "http://localhost:3030";
    private final String CATALOG_URL  = "http://localhost:3031";
    private final String INDEX_URL    = "http://localhost:3032";
    private final String CRAWLER_URL  = "http://localhost:3033";
    private final String PARSER_URL   = "http://localhost:3034";
    private final String SEARCHER_URL = "http://localhost:3035";
    private final String UPDATER_URL  = "http://localhost:3037";

    @Resource(name = "roundTripTimeMonitor")
    private RoundTripTimeMonitor rttMonitor;

    @Resource(name = "messagesPerSecondMonitor")
    private MessagesPerSecondMonitor mpsMonitor;

    private final RestTemplate restTemplate = new RestTemplate();

    private final BenchmarkMonitor reportMonitor = new BenchmarkMonitor(ArchitectureType.ECHO_MSA);
    private final BenchmarkUtil benchmarkUtil = new BenchmarkUtil("../../benchmark/");

    @RequestMapping(
        value  = "/rtt-report",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE)
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

    /*
    @RequestMapping(
        value  = "/mps-report",
        method = RequestMethod.POST,
        params = { "name" })
    public ResponseEntity<Void> mpsReport(@RequestParam("name") String name, @RequestBody MessagesPerSecondResult mps) throws URISyntaxException {
        log.debug("REST request to report {} MPS : {}", name, mps);
        mpsMonitor.addMetric(name, mps.getMps()); // TODO
        if (mpsMonitor.isFinished()) {
            log.info("MPS reporting finished; results in CSV format :");
            System.out.println(mpsMonitor.toCsv());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/cpu-report",
        method = RequestMethod.POST,
        params = { "name" })
    public ResponseEntity<Void> cpuReport(@RequestParam("name") String name, @RequestBody CpuLoadResult cpuLoadResult) throws URISyntaxException {
        log.debug("REST request to report {} CPU load : {}", name, cpuLoadResult);

        // TODO
        log.info("{} : {}", name, cpuLoadResult.getMeanLoadStr());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/memory-report",
        method = RequestMethod.POST,
        params = { "name" })
    public ResponseEntity<Void> memoryReport(@RequestParam("name") String name, @RequestBody MemoryUsageResult memoryUsageResult) throws URISyntaxException {
        log.debug("REST request to report {} memory load : {}", name, memoryUsageResult);

        // TODO
        log.info("{} : {}", name, memoryUsageResult.getMeanBytesStr());

        return new ResponseEntity<>(HttpStatus.OK);
    }
    */

    @RequestMapping(
        value  = "/benchmark-report",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> benchmarkReport(@RequestBody BenchmarkMeterReport report) throws URISyntaxException {
        log.info("REST request to report benchmark results for : {}", report.getName());
        log.debug("{}", report);

        /* for Microservices, the MPS reports are equal to overall reports, so no special handling I guess?
        mpsMonitor.addMetric(report.getName(), report.getMps().getMps());
        System.out.println(report.getName() + "\t: " + report.getMemoryUsage().getMeanBytesStr());
        System.out.println(report.getName() + "\t: " + report.getCpuLoad().getMeanLoadStr());
        if (mpsMonitor.isFinished()) {
            log.info("MPS reporting finished; results in CSV format :");
            System.out.println(mpsMonitor.toCsv());
        }
        */

        reportMonitor.addReport(report);
        if (reportMonitor.isFinished()) {
            log.info("All expected benchmark reports received :");
            System.out.println(reportMonitor.toCsv());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/monitor-feed-progress",
        method = RequestMethod.POST)
    public ResponseEntity<Void> monitorFeedProgress(@RequestBody List<FeedProperty> feedProperties) throws URISyntaxException {
        log.info("REST request to monitor fed progress for properties list : {}", feedProperties);
        rttMonitor.initWithProperties(ImmutableList.copyOf(feedProperties));
        reportMonitor.init(7);
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
        reportMonitor.init(7);
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
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
        value  = "/stop-mps",
        method = RequestMethod.POST,
        params = { "mps" })
    public ResponseEntity<Void> stopMpsCounting(@RequestParam("mps") @SuppressWarnings("unused") Boolean mps) throws URISyntaxException {
        log.debug("REST request to stop MPS counting");
        sendStopMessagePerSecondMonitoringMessages();
        return new ResponseEntity<>(HttpStatus.OK);
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
        final String url = GATEWAY_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Gateway
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startCatalogMps(boolean mps) {
        final String url = CATALOG_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Catalog
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startIndexMps(boolean mps) {
        final String url = INDEX_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Index
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startCrawlerMps(boolean mps) {
        final String url = CRAWLER_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Crawler
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startParserMps(boolean mps) {
        final String url = PARSER_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Parser
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startSearcherMps(boolean mps) {
        final String url = SEARCHER_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Searcher
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void startUpdaterMps(boolean mps) {
        final String url = UPDATER_URL+"/benchmark/start-benchmark-meters?mps="+mps; // Updater
        log.info("Sending start MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopGatewayMps(boolean mps) {
        final String url = GATEWAY_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Gateway
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopCatalogMps(boolean mps) {
        final String url = CATALOG_URL+"/benchmark/stop-benchmark-meters?mps="+mps;  // Catalog
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopIndexMps(boolean mps) {
        final String url = INDEX_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Index
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopCrawlerMps(boolean mps) {
        final String url = CRAWLER_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Crawler
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopParserMps(boolean mps) {
        final String url = PARSER_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Parser
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopSearcherMps(boolean mps) {
        final String url = SEARCHER_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Searcher
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

    @Async
    public void stopUpdaterMps(boolean mps) {
        final String url = UPDATER_URL+"/benchmark/stop-benchmark-meters?mps="+mps; // Updater
        log.info("Sending stop MPS meter to : {}", url);
        restTemplate.postForObject(url, null, Void.class);
    }

}
