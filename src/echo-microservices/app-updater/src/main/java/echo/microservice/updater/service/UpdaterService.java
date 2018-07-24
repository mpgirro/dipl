package echo.microservice.updater.service;

import echo.core.async.catalog.ImmutableProposeNewFeedJob;
import echo.core.async.catalog.ProposeNewFeedJob;
import echo.core.async.crawler.ImmutableNewFeedCrawlerJob;
import echo.core.async.crawler.NewFeedCrawlerJob;
import echo.core.async.updater.*;
import echo.core.benchmark.RoundTripTime;
import echo.microservice.updater.async.CatalogQueueSender;
import echo.microservice.updater.async.CrawlerQueueSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Maximilian Irro
 */
@Service
public class UpdaterService {

    private final Logger log = LoggerFactory.getLogger(UpdaterService.class);

    @Autowired
    private CrawlerQueueSender crawlerQueueSender;

    @Autowired
    private CatalogQueueSender catalogQueueSender;

    @Async
    public void proposeNewFeed(String feed, RoundTripTime rtt) {
        final ProposeNewFeedJob job = ImmutableProposeNewFeedJob.of(feed, rtt.bumpRTTs());
        catalogQueueSender.produceMsg(job);
    }

    @Async
    public void processJob(ProposeNewFeedJob job) {
        log.info("Recieved ProposeNewFeedJob for feed : {}", job.getFeed());
        proposeNewFeed(job.getFeed(), job.getRtt());
    }

    @Async
    public void processNewFeedJob(ProcessNewFeedJob updaterJob) {
        log.info("Recieved ProcessNewFeedJob for EXO : {}", updaterJob.getExo());
        final NewFeedCrawlerJob crawlerJob = ImmutableNewFeedCrawlerJob.of(updaterJob.getExo(), updaterJob.getFeed(), updaterJob.getRtt().bumpRTTs());
        crawlerQueueSender.produceMsg(crawlerJob);
    }

    @Async
    public void processJob(ProcessUpdateFeedJob job) {
        log.info("Recieved ProcessUpdateFeedJob for EXO : {}", job.getExo());
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Async
    public void processJob(ProcessFeedWebsiteJob job) {
        log.info("Recieved ProcessFeedWebsiteJob for EXO : {}", job.getExo());
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Async
    public void processJob(UpdaterJob job) {
        throw new RuntimeException("Received unhandled UpdaterJob of type : " + job.getClass());
    }
}
