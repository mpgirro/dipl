package echo.microservice.crawler.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

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

    public void produceMsg(String msg){
        amqpTemplate.convertAndSend(exchange, routingKey, msg);
        log.info("Send msg = " + msg);
    }

}
