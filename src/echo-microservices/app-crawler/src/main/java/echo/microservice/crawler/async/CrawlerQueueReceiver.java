package echo.microservice.crawler.async;

import echo.core.async.job.CrawlerJob;
import echo.core.async.job.NewFeedCrawlerJob;
import echo.microservice.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Maximilian Irro
 */
@Component
public class CrawlerQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(CrawlerQueueReceiver.class);

    @Autowired
    private CrawlerService crawlerService;

    /*
    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.crawler-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.crawler.routingkey")
    )
    public void recievedMessage(NewFeedCrawlerJob job) {
        log.info("Recieved Message : {}", job);
        crawlerService.downloadFeed(job.getExo(), job.getUrl());
    }
    */

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.crawler-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.crawler.routingkey")
    )
    public void recievedMessage(CrawlerJob job) {
        log.info("Recieved Message : {}", job);
        if (job instanceof NewFeedCrawlerJob) {
            crawlerService.downloadFeed(job.getExo(), job.getUrl());
        } else {
            throw new RuntimeException("Received unhandled CrawlerJob of type : " + job.getClass());
        }
    }

}
