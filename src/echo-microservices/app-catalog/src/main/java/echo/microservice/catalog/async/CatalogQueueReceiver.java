package echo.microservice.catalog.async;

import echo.core.async.job.*;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.mapper.IndexMapper;
import echo.microservice.catalog.service.EpisodeService;
import echo.microservice.catalog.service.PodcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Component
public class CatalogQueueReceiver {

    private final Logger log = LoggerFactory.getLogger(CatalogQueueReceiver.class);

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private EpisodeService episodeService;

    @Autowired
    private IndexQueueSender indexQueueSender;

    private final IndexMapper indexMapper = IndexMapper.INSTANCE;

    /*
    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.catalog-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.catalog.routingkey")
    )
    public void recievedMessage(UpdatePodcastCatalogJob catalogJob) {
        log.info("Recieved Message : {}", catalogJob);
        final Optional<PodcastDTO> registered = podcastService.save(catalogJob.getPodcast());
        if (registered.isPresent()) {
            final AddOrUpdateDocIndexJob indexJob = new AddOrUpdateDocIndexJob(indexMapper.toImmutable(registered.get()));
            indexQueueSender.produceMsg(indexJob);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.catalog-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.catalog.routingkey")
    )
    public void recievedMessage(EpisodeRegisterJob job) {
        log.info("Recieved Message : {}", job);
        episodeService.register(job);
    }
    */

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "echo.rabbit.catalog-queue", durable = "true"),
        exchange = @Exchange(value = "echo.direct", durable = "true"),
        key      = "echo.catalog.routingkey")
    )
    public void recievedMessage(CatalogJob job) {
        log.info("Recieved Message : {}", job);
        if (job instanceof UpdatePodcastCatalogJob) {
            final Optional<PodcastDTO> registered = podcastService.update(((UpdatePodcastCatalogJob) job).getPodcast());
            if (registered.isPresent()) {
                final AddOrUpdateDocIndexJob indexJob = new AddOrUpdateDocIndexJob(indexMapper.toImmutable(registered.get()));
                indexQueueSender.produceMsg(indexJob);
            }
        } else if (job instanceof EpisodeRegisterJob) {
            episodeService.register((EpisodeRegisterJob) job);
        } else {
            throw new RuntimeException("Received unhandled CatalogJob of type : " + job.getClass());
        }
    }

}
