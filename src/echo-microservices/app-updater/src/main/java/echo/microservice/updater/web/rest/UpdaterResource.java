package echo.microservice.updater.web.rest;

import echo.core.async.catalog.ProposeNewFeedJob;
import echo.core.benchmark.MessagesPerSecondMeter;
import echo.microservice.updater.service.UpdaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/updater")
public class UpdaterResource {

    private final Logger log = LoggerFactory.getLogger(UpdaterResource.class);

    @Autowired
    private UpdaterService updaterService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value  = "/propose-new-feed",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void proposeNewFeed(@RequestBody ProposeNewFeedJob job) {
        log.debug("REST request to propose new feed: ('{}',_)", job.getFeed());
        mpsMeter.incrementCounter();
        updaterService.proposeNewFeed(job.getFeed(), job.getRtt());
    }

}
