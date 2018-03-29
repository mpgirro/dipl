package echo.microservice.parser.async;

import echo.microservice.parser.service.ParserService;
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
public class ParserQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(ParserQueueReceiver.class);

    @Autowired
    private ParserService parserService;

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.parser-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.parser.routingkey")
    )
    public void recievedMessage(String msg) {
        log.info("Recieved Message: " + msg);
        // parserService.parseFeed(TODO);
    }

}
