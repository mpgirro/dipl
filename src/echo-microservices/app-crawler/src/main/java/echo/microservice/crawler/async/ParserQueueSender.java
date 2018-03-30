package echo.microservice.crawler.async;

import echo.core.async.job.ParserJob;
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
public class ParserQueueSender {

    private final Logger log = LoggerFactory.getLogger(ParserQueueSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${echo.amqp.exchange}")
    private String exchange;

    @Value("${echo.amqp.parser-routingkey}")
    private String routingKey;

    public void produceMsg(ParserJob job){
        amqpTemplate.convertAndSend(exchange, routingKey, job);
        log.info("Send msg : {}", job);
    }

}
