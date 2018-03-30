package echo.microservice.index.async;

import echo.core.async.job.AddOrUpdateDocIndexJob;
import echo.core.async.job.IndexJob;
import echo.microservice.index.service.IndexService;
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
public class IndexQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(IndexQueueReceiver.class);

    @Autowired
    private IndexService indexService;

    /*
    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.index-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.index.routingkey")
    )
    public void recievedMessage(AddOrUpdateDocIndexJob job) {
        log.info("Recieved Message : {}", job);
        indexService.add(job.getIndexDoc()); // TODO replace add with addOrUpdate
    }
    */

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.index-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.index.routingkey")
    )
    public void recievedMessage(IndexJob job) {
        log.info("Recieved Message : {}", job);
        if (job instanceof AddOrUpdateDocIndexJob) {
            indexService.add(((AddOrUpdateDocIndexJob) job).getIndexDoc()); // TODO replace add with addOrUpdate
        } else {
            throw new RuntimeException("Received unhandled IndexJob of type : " + job.getClass());
        }
    }

}
