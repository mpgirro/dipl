package echo.microservice.catalog.service;

import com.google.common.base.MoreObjects;
import echo.core.async.crawler.ImmutableNewFeedCrawlerJob;
import echo.core.async.crawler.NewFeedCrawlerJob;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.ImmutableFeedDTO;
import echo.core.domain.dto.ImmutablePodcastDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.feed.FeedStatus;
import echo.core.mapper.FeedMapper;
import echo.microservice.catalog.ExoUtil;
import echo.microservice.catalog.async.CrawlerQueueSender;
import echo.microservice.catalog.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
@Service
@Transactional
public class FeedService {

    private final Logger log = LoggerFactory.getLogger(FeedService.class);

    @Value("${echo.catalog.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.catalog.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private CrawlerQueueSender crawlerQueueSender;

    private final FeedMapper feedMapper = FeedMapper.INSTANCE;

    @Transactional
    public Optional<FeedDTO> save(FeedDTO feedDTO) {
        log.debug("Request to save Feed : {}", feedDTO);
        return Optional.of(feedDTO)
            .map(feedMapper::toModifiable)
            .map(feedMapper::toEntity)
            .map(feedRepository::save)
            .map(feedMapper::toImmutable);
    }

    @Transactional
    public Optional<FeedDTO> update(FeedDTO feed) {
        log.debug("Request to update Feed : {}", feed);
        return findOneByExo(feed.getExo())
            .map(feedMapper::toModifiable)
            .map(f -> {
                final Long id = f.getId();
                feedMapper.update(feed, f);
                f.setId(id);
                return save(f);
            })
            .orElse(Optional.empty());
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOne(Long id) {
        log.debug("Request to get Feed (ID) : {}", id);
        return Optional
            .ofNullable(feedRepository.findOne(id))
            .map(feedMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByExo(String exo) {
        log.debug("Request to get Feed (EXO) : {}", exo);
        return Optional
            .ofNullable(feedRepository.findOneByExo(exo))
            .map(feedMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAll(Integer page, Integer size) {
        log.debug("Request to get all Feeds by page : {} and size : {}", page, size);
        final PageRequest pageable = getPageable(page, size);
        return feedRepository.findAll(pageable).getContent().stream()
            .map(feedMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAllByUrl(String url) {
        log.debug("Request to get all Feeds by URL : {}", url);
        return feedRepository.findAllByUrl(url).stream()
            .map(feedMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByUrlAndPodcastEchoId(String url, String podcastExo) {
        log.debug("Request to get all Feeds by URL : {} and Podcast (EXO) : ", url, podcastExo);
        return Optional
            .ofNullable(feedRepository.findOneByUrlAndPodcastExo(url, podcastExo))
            .map(feedMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAllByPodcast(String podcastExo) {
        log.debug("Request to get all Feeds by Podcast (EXO) : ", podcastExo);
        return feedRepository.findAllByPodcast(podcastExo).stream()
            .map(feedMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        log.debug("Request to count all Feeds");
        return feedRepository.countAll();
    }

    @Transactional
    public void propose(String feedUrl) {
        log.debug("Request to propose Feed by URL : {}", feedUrl);
        if (findAllByUrl(feedUrl).isEmpty()) {

            // TODO for now we always create a podcast for an unknown feed, but we will have to check if the feed is an alternate to a known podcast

            final ImmutablePodcastDTO.Builder pBuilder = ImmutablePodcastDTO.builder();
            pBuilder
                .setExo(ExoUtil.getInstance().getExoGenerator().getNewExo())
                .setDescription(feedUrl)
                .setRegistrationComplete(true)
                .setRegistrationTimestamp(LocalDateTime.now());
            final Optional<PodcastDTO> podcast = podcastService.save(pBuilder.create());

            if (podcast.isPresent()) {
                final PodcastDTO p = podcast.get();

                final ImmutableFeedDTO.Builder fBuilder = ImmutableFeedDTO.builder();
                fBuilder
                    .setExo(ExoUtil.getInstance().getExoGenerator().getNewExo())
                    .setPodcastId(p.getId())
                    .setUrl(feedUrl)
                    .setLastChecked(LocalDateTime.now())
                    .setLastStatus(FeedStatus.NEVER_CHECKED)
                    .setRegistrationTimestamp(LocalDateTime.now());
                save(fBuilder.create());

                final NewFeedCrawlerJob job = ImmutableNewFeedCrawlerJob.of(p.getExo(), feedUrl);
                crawlerQueueSender.produceMsg(job);
            } else {
                log.error("Error on saving podcast (from builder) : {}", pBuilder);
            }
        } else {
            log.info("Proposed feed is already in database: {}", feedUrl);
        }

    }

    private PageRequest getPageable(Integer page, Integer size) {
        final int p = MoreObjects.firstNonNull(page, DEFAULT_PAGE) - 1;
        final int s = MoreObjects.firstNonNull(size, DEFAULT_SIZE);

        if (p < 0) {
            // TODO throw exception
            // TODO in the Actors version, this is NOT done in the service --> should it better be?
        }

        if (s < 0) {
            // TODO throw exception
            // TODO in the Actors version, this is NOT done in the service --> should it better be?
        }

        return new PageRequest(page, size);
    }

}
