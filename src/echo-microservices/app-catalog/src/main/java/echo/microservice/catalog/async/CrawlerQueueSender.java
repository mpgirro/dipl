package echo.microservice.catalog.async;

import echo.core.async.crawler.CrawlerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Maximilian Irro
 */
@Component
public class CrawlerQueueSender {

    private final Logger log = LoggerFactory.getLogger(CrawlerQueueSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${echo.amqp.exchange}")
    private String exchange;

    @Value("${echo.amqp.crawler-routingkey}")
    private String routingKey;

    public void produceMsg(CrawlerJob job){
        amqpTemplate.convertAndSend(exchange, routingKey, job);
        log.info("Send msg : {}", job);
    }

}
