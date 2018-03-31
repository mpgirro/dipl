package echo.microservice.parser.async;

import echo.core.async.parser.NewFeedParserJob;
import echo.core.async.parser.ParserJob;
import echo.core.async.parser.UpdateFeedParserJob;
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
    public void recievedMessage(ParserJob parserJob) {
        log.debug("Recieved Message : {}", parserJob);
        if (parserJob instanceof NewFeedParserJob) {
            final NewFeedParserJob job = (NewFeedParserJob) parserJob;
            parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), true);
        } else if (parserJob instanceof UpdateFeedParserJob) {
            final UpdateFeedParserJob job = (UpdateFeedParserJob) parserJob;
            parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), false);
        } else {
            throw new RuntimeException("Received unhandled ParserJob of type : " + parserJob.getClass());
        }
    }

}
