package echo.microservice.updater.async;

import echo.core.async.updater.UpdaterJob;
import echo.core.benchmark.MessagesPerSecondCounter;
import echo.microservice.updater.service.UpdaterService;
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
public class UpdaterQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(UpdaterQueueReceiver.class);

    @Autowired
    private UpdaterService updaterService;

    @Resource(name = "messagesPerSecondCounter")
    private MessagesPerSecondCounter mpsCounter;

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "${echo.rabbit.updater-queue}", durable = "true"),
        exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
        key      = "${echo.amqp.updater-routingkey}")
    )
    public void recievedMessage(UpdaterJob job) {
        //log.debug("Recieved Message : {}", job);
        mpsCounter.incrementCounter();
        updaterService.processJob(job);
        /*
        if (updaterJob instanceof ProcessNewFeedJob) {
            final ProcessNewFeedJob job = (ProcessNewFeedJob) updaterJob;
            log.info("Recieved ProcessNewFeedJob for EXO : {}", job.getExo());
            parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), true);
        } else if (updaterJob instanceof ProcessUpdateFeedJob) {
            final ProcessUpdateFeedJob job = (ProcessUpdateFeedJob) updaterJob;
            log.info("Recieved ProcessUpdateFeedJob for EXO : {}", job.getExo());
            // TODO
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (updaterJob instanceof ProcessFeedWebsiteJob) {
            final ProcessFeedWebsiteJob job = (ProcessFeedWebsiteJob) updaterJob;
            log.info("Recieved ProcessFeedWebsiteJob for EXO : {}", job.getExo());
            // TODO
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            throw new RuntimeException("Received unhandled UpdaterJob of type : " + updaterJob.getClass());
        }
        */
    }

}
