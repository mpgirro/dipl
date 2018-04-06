package echo.microservice.catalog.async;

import echo.core.async.index.IndexJob;
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
public class IndexQueueSender {

    private final Logger log = LoggerFactory.getLogger(IndexQueueSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${echo.amqp.exchange}")
    private String exchange;

    @Value("${echo.amqp.index-routingkey}")
    private String routingKey;

    public void produceMsg(IndexJob job){
        //log.debug("Send msg : {}", job);
        amqpTemplate.convertAndSend(exchange, routingKey, job);
    }

}
