package echo.microservice.catalog.async;

import echo.core.async.catalog.CatalogJob;
import echo.core.async.catalog.ProposeNewFeedJob;
import echo.core.async.catalog.RegisterEpisodeIfNewJobCatalogJob;
import echo.core.async.catalog.UpdatePodcastCatalogJob;
import echo.core.async.index.AddOrUpdateDocIndexJob;
import echo.core.async.index.ImmutableAddOrUpdateDocIndexJob;
import echo.core.domain.dto.PodcastDTO;
import echo.core.mapper.IndexMapper;
import echo.microservice.catalog.service.EpisodeService;
import echo.microservice.catalog.service.FeedService;
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
    private FeedService feedService;

    @Autowired
    private IndexQueueSender indexQueueSender;

    private final IndexMapper indexMapper = IndexMapper.INSTANCE;

    @RabbitListener(bindings = @QueueBinding(
        value    = @Queue(value = "${echo.rabbit.catalog-queue}", durable = "true"),
        exchange = @Exchange(value = "${echo.amqp.exchange}", durable = "true"),
        key      = "${echo.amqp.catalog-routingkey}")
    )
    public void recievedMessage(CatalogJob catalogJob) {
        log.debug("Recieved Message : {}", catalogJob);
        mpsCounter.incrementCounter();
        if (catalogJob instanceof UpdatePodcastCatalogJob) {
            final UpdatePodcastCatalogJob job = (UpdatePodcastCatalogJob) catalogJob;
            log.debug("Recieved UpdatePodcastCatalogJob for EXO : {}", job.getPodcast().getExo());
            final Optional<PodcastDTO> registered = podcastService.update(job.getPodcast());
            if (registered.isPresent()) {
                final AddOrUpdateDocIndexJob indexJob = ImmutableAddOrUpdateDocIndexJob.of(indexMapper.toImmutable(registered.get()), job.getRTT().bumpRTTs());
                indexQueueSender.produceMsg(indexJob);
            }
        } else if (catalogJob instanceof RegisterEpisodeIfNewJobCatalogJob) {
            final RegisterEpisodeIfNewJobCatalogJob job = (RegisterEpisodeIfNewJobCatalogJob) catalogJob;
            log.debug("Recieved RegisterEpisodeIfNewJobCatalogJob for Podcast EXO : {}", job.getPodcastExo());
            episodeService.register((RegisterEpisodeIfNewJobCatalogJob) catalogJob);
        } else if (catalogJob instanceof ProposeNewFeedJob) {
            final ProposeNewFeedJob job = (ProposeNewFeedJob) catalogJob;
            log.debug("Recieved ProposeNewFeedJob for feed : {}", job.getFeed());
            feedService.propose(job.getFeed(), job.getRTT());
        } else {
            throw new RuntimeException("Received unhandled CatalogJob of type : " + catalogJob.getClass());
        }
    }

}
