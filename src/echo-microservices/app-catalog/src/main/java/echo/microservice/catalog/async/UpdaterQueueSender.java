package echo.microservice.catalog.async;

import echo.core.async.updater.UpdaterJob;
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
public class UpdaterQueueSender {

    private final Logger log = LoggerFactory.getLogger(UpdaterQueueSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${echo.amqp.exchange}")
    private String exchange;

    @Value("${echo.amqp.updater-routingkey}")
    private String routingKey;

    public void produceMsg(UpdaterJob job){
        amqpTemplate.convertAndSend(exchange, routingKey, job);
    }

}
