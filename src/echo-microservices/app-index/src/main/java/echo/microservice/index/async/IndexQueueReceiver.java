package echo.microservice.index.async;

import echo.core.async.index.AddOrUpdateDocIndexJob;
import echo.core.async.index.IndexJob;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.microservice.index.service.BenchmarkService;
import echo.microservice.index.service.IndexService;
import echo.microservice.index.web.client.BenchmarkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Maximilian Irro
 */
@Component
public class IndexQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(IndexQueueReceiver.class);

    @Autowired
    private IndexService indexService;

    @Autowired
    private BenchmarkService benchmarkService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RabbitListener(
        //containerFactory = "rabbitListenerContainerFactory",
        bindings = @QueueBinding(
            value    = @Queue(value = "${echo.rabbit.index-queue}", durable = "true"),
            exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
            key      = "${echo.amqp.index-routingkey}")
    )
    public void recievedMessage(IndexJob indexJob) {
        mpsMeter.incrementCounter();
        if (indexJob instanceof AddOrUpdateDocIndexJob) {
            final AddOrUpdateDocIndexJob job = (AddOrUpdateDocIndexJob) indexJob;
            log.debug("Recieved AddOrUpdateDocIndexJob for EXO : {}", job.getIndexDoc().getExo());
            indexService.add(job.getIndexDoc()); // TODO replace add with addOrUpdate
            benchmarkService.sendRttReport(job.getRtt().bumpRTTs());
        } else {
            throw new RuntimeException("Received unhandled IndexJob of type : " + indexJob.getClass());
        }
    }



}
