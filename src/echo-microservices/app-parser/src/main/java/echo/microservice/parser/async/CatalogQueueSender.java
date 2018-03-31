package echo.microservice.parser.async;

import echo.core.async.catalog.CatalogJob;
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
public class CatalogQueueSender {

    private final Logger log = LoggerFactory.getLogger(CatalogQueueSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${echo.amqp.exchange}")
    private String exchange;

    @Value("${echo.amqp.catalog-routingkey}")
    private String routingKey;

    public void produceMsg(CatalogJob job){
        amqpTemplate.convertAndSend(exchange, routingKey, job);
        log.info("Send msg : {}", job);
    }

}
