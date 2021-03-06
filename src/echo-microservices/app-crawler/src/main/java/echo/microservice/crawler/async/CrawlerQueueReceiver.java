package echo.microservice.crawler.async;

import echo.core.async.crawler.CrawlerJob;
import echo.core.async.crawler.NewFeedCrawlerJob;
import echo.core.async.crawler.UpdateFeedCrawlerJob;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.microservice.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Maximilian Irro
 */
@Component
public class CrawlerQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(CrawlerQueueReceiver.class);

    @Autowired
    private CrawlerService crawlerService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        bindings = @QueueBinding(
            value    = @Queue(value = "${echo.rabbit.crawler-queue}", durable = "true"),
            exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
            key      = "${echo.amqp.crawler-routingkey}")
    )
    public void receive(CrawlerJob crawlerJob) {
        mpsMeter.tick();
        if (crawlerJob instanceof NewFeedCrawlerJob) {
            final NewFeedCrawlerJob job = (NewFeedCrawlerJob) crawlerJob;
            log.info("Recieved NewFeedCrawlerJob for Podcast EXO : {}", job.exo());
            crawlerService.downloadFeed(job.exo(), job.url(), true, job.getRtt());
        } else if (crawlerJob instanceof UpdateFeedCrawlerJob) {
            final UpdateFeedCrawlerJob job = (UpdateFeedCrawlerJob) crawlerJob;
            log.info("Recieved UpdateFeedCrawlerJob for Podcast EXO : {}", job.exo());
            crawlerService.downloadFeed(job.exo(), job.url(), false, job.getRtt());
        } else {
            throw new RuntimeException("Received unhandled CrawlerJob of type : " + crawlerJob.getClass());
        }
    }

}
