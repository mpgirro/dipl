package echo.microservice.catalog.service;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.entity.Feed;
import echo.core.mapper.FeedMapper;
import echo.microservice.catalog.repository.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private FeedMapper feedMapper = FeedMapper.INSTANCE;

    @Transactional
    public Optional<FeedDTO> save(FeedDTO feedDTO) {
        log.debug("Request to save Feed : {}", feedDTO);
        final Feed feed = feedMapper.map(feedDTO);
        final Feed result = feedRepository.save(feed);
        return Optional.of(feedMapper.map(result));
    }

    @Transactional
    public Optional<FeedDTO> update(FeedDTO feedDTO) {
        log.debug("Request to update Feed : {}", feedDTO);
        return findOneByEchoId(feedDTO.getEchoId())
            .map(feed -> {
                final long id = feed.getId();
                feedMapper.update(feedDTO, feed);
                feed.setId(id);
                return save(feed);
            })
            .orElse(Optional.empty());
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOne(Long id) {
        log.debug("Request to get Feed (ID) : {}", id);
        final Feed result = feedRepository.findOne(id);
        return Optional.ofNullable(feedMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Feed (EXO) : {}", exo);
        final Feed result = feedRepository.findOneByEchoId(exo);
        return Optional.ofNullable(feedMapper.map(result));
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAll(Integer page, Integer size) {
        log.debug("Request to get all Feeds by page : {} and size : {}", page, size);
        final PageRequest pageable = getPageable(page, size);
        return feedRepository.findAll(pageable).getContent().stream()
            .map(feedMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAllByUrl(String url) {
        log.debug("Request to get all Feeds by URL : {}", url);
        return feedRepository.findAllByUrl(url).stream()
            .map(feedMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<FeedDTO> findOneByUrlAndPodcastEchoId(String url, String podcastExo) {
        log.debug("Request to get all Feeds by URL : {} and Podcast (EXO) : ", url, podcastExo);
        final Feed result = feedRepository.findOneByUrlAndPodcastEchoId(url, podcastExo);
        return Optional.ofNullable(feedMapper.map(result));
    }

    @Transactional(readOnly = true)
    public List<FeedDTO> findAllByPodcast(String podcastExo) {
        log.debug("Request to get all Feeds by Podcast (EXO) : ", podcastExo);
        return feedRepository.findAllByUrl(podcastExo).stream()
            .map(feedMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        log.debug("Request to count all Feeds");
        return feedRepository.countAll();
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
