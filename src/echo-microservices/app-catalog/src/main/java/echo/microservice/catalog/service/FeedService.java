package echo.microservice.catalog.service;

import com.google.common.base.MoreObjects;
import echo.core.async.job.NewFeedCrawlerJob;
import echo.core.domain.dto.*;
import echo.core.domain.entity.FeedEntity;
import echo.core.domain.feed.FeedStatus;
import echo.core.mapper.FeedMapper;
import echo.core.util.ExoGenerator;
import echo.microservice.catalog.async.CrawlerQueueSender;
import echo.microservice.catalog.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
@Transactional
public class FeedService {

    private final Logger log = LoggerFactory.getLogger(FeedService.class);

    private final String CRAWLER_URL = "http://localhost:3033"; // TODO

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

    private final ExoGenerator exoGenerator = new ExoGenerator(1); // TODO set the microservice worker count

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public Optional<FeedDTO> save(FeedDTO feedDTO) {
        log.debug("Request to save Feed : {}", feedDTO);
        final ModifiableFeedDTO f = feedMapper.toModifiable(feedDTO);
        if (isNullOrEmpty(f.getEchoId())) {
            f.setEchoId(exoGenerator.getNewExo());
        }
        final FeedEntity feed = feedMapper.toEntity(f);
        final FeedEntity result = feedRepository.save(feed);
        return Optional.of(feedMapper.toImmutable(result));
    }

    @Transactional
    public Optional<FeedDTO> update(FeedDTO feedDTO) {
        log.debug("Request to update Feed : {}", feedDTO);
        return findOneByEchoId(feedDTO.getEchoId())
            .map(feedMapper::toModifiable)
            .map(feed -> {
                final Long id = feed.getId();
                feedMapper.update(feedDTO, feed);
                feed.setId(id);
                return save(feed);
            })
            .orElse(Optional.empty());
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOne(Long id) {
        log.debug("Request to get Feed (ID) : {}", id);
        final FeedEntity result = feedRepository.findOne(id);
        return Optional.ofNullable(feedMapper.toImmutable(result));
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Feed (EXO) : {}", exo);
        final FeedEntity result = feedRepository.findOneByEchoId(exo);
        return Optional.ofNullable(feedMapper.toImmutable(result));
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
        final FeedEntity result = feedRepository.findOneByUrlAndPodcastEchoId(url, podcastExo);
        return Optional.ofNullable(feedMapper.toImmutable(result));
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
                .setDescription(feedUrl)
                .setRegistrationComplete(true)
                .setRegistrationTimestamp(LocalDateTime.now());
            final PodcastDTO podcast = podcastService.save(pBuilder.create()).get();

            final ImmutableFeedDTO.Builder fBuilder = ImmutableFeedDTO.builder();
            fBuilder
                .setPodcastId(podcast.getId())
                .setUrl(feedUrl)
                .setLastChecked(LocalDateTime.now())
                .setLastStatus(FeedStatus.NEVER_CHECKED)
                .setRegistrationTimestamp(LocalDateTime.now());
            save(fBuilder.create());

            // TODO
            //crawlerQueueSender.produceMsg("<Fetch-Feed : " + feedUrl + ">");
            final NewFeedCrawlerJob job = new NewFeedCrawlerJob(podcast.getEchoId(), feedUrl);
            crawlerQueueSender.produceMsg(job);

            /*
            // TODO send url to crawler for download
            // TODO replace by sending job to queue
            final String parserUrl = CRAWLER_URL+"/crawler/download-feed?exo="+podcast.getEchoId()+"&url="+feedUrl;
            final HttpEntity<String> request = new HttpEntity<>(""); // TODO dummy, we do not send a body that should be created (as is custom with POST)
            final ResponseEntity<String> response = restTemplate.exchange(parserUrl, HttpMethod.POST, request, String.class);
            */
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
