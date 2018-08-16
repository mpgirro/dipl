package echo.microservice.parser.async;

import echo.core.async.parser.NewFeedParserJob;
import echo.core.async.parser.ParserJob;
import echo.core.async.parser.UpdateFeedParserJob;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.microservice.parser.service.ParserService;
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
public class ParserQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(ParserQueueReceiver.class);

    @Autowired
    private ParserService parserService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        bindings = @QueueBinding(
            value    = @Queue(value = "${echo.rabbit.parser-queue}", durable = "true"),
            exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
            key      = "${echo.amqp.parser-routingkey}")
    )
    public void recievedMessage(ParserJob parserJob) {
        //log.debug("Recieved Message : {}", parserJob);
        mpsMeter.tick();
        if (parserJob instanceof NewFeedParserJob) {
            final NewFeedParserJob job = (NewFeedParserJob) parserJob;
            log.info("Recieved NewFeedParserJob for EXO : {}", job.getExo());
            parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), true, job.getRtt());
        } else if (parserJob instanceof UpdateFeedParserJob) {
            final UpdateFeedParserJob job = (UpdateFeedParserJob) parserJob;
            log.info("Recieved UpdateFeedParserJob for EXO : {}", job.getExo());
            parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), false, job.getRtt());
        } else {
            throw new RuntimeException("Received unhandled ParserJob of type : " + parserJob.getClass());
        }
    }

}
