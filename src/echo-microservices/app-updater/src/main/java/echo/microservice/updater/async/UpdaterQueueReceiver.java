package echo.microservice.updater.async;

import echo.core.async.updater.*;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
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

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RabbitListener(
        containerFactory = "rabbitListenerContainerFactory",
        bindings = @QueueBinding(
            value    = @Queue(value = "${echo.rabbit.updater-queue}", durable = "true"),
            exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
            key      = "${echo.amqp.updater-routingkey}")
    )
    public void receive(UpdaterJob updaterJob) {
        log.debug("Received Message : {}", updaterJob);
        mpsMeter.tick();
        if (updaterJob instanceof ProcessNewFeedJob) {
            final ProcessNewFeedJob job = (ProcessNewFeedJob) updaterJob;
            log.info("Recieved ProcessNewFeedJob for EXO : {}", job.getExo());
            updaterService.processNewFeedJob(job);
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
        } else if (updaterJob instanceof BenchmarkProposeNewFeedJob) {
            final BenchmarkProposeNewFeedJob job = (BenchmarkProposeNewFeedJob) updaterJob;
            log.info("Recieved BenchmarkProposeNewFeedJob for feed : {}", job.getFeed());
            mpsMeter.tick();
            updaterService.proposeNewFeed(job.getFeed(), job.getRtt());
        } else {
            throw new RuntimeException("Received unhandled UpdaterJob of type : " + updaterJob.getClass());
        }
    }

}
