package echo.microservice.registry.web.rest;

import com.google.common.collect.ImmutableList;
import echo.core.benchmark.FeedProperty;
import echo.core.benchmark.MessagesPerSecondCounter;
import echo.core.benchmark.MessagesPerSecondMonitor;
import echo.core.benchmark.RoundTripTimeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    @Resource(name = "roundTripTimeMonitor")
    private RoundTripTimeMonitor rttMonitor;

    @Resource(name = "messagesPerSecondMonitor")
    private MessagesPerSecondMonitor mpsMonitor;

    @RequestMapping(
        value  = "//mps-report",
        method = RequestMethod.POST,
        params = { "name", "mps" })
    @ResponseStatus(HttpStatus.OK)
    public void startMpsCounting(@RequestParam("name") String name, @RequestParam("mps") Double mps) throws URISyntaxException {
        log.debug("REST request to report MPS : {} for unit : {}", mps, name);
        mpsMonitor.addAndPrintMetric(name, mps);
    }


    @RequestMapping(
        value  = "/monitor-feed-progress",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void monitorFeedProgress(@RequestBody ImmutableList<FeedProperty> feedProperties) throws URISyntaxException {
        log.debug("REST request to monitor fed progress for properties list");
        // TODO
    }

    @RequestMapping(
        value  = "/monitor-query-progress",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void monitorQueryProgress(@RequestBody ImmutableList<String> queries) throws URISyntaxException {
        log.debug("REST request to monitor fed progres for queries list");
        // TODO
    }

}
