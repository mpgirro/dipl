package echo.microservice.catalog.service;

import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.Podcast;
import echo.core.domain.feed.FeedStatus;
import echo.core.mapper.PodcastMapper;
import echo.core.mapper.TeaserMapper;
import echo.microservice.catalog.repository.PodcastRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PodcastService {

    private final Logger log = LoggerFactory.getLogger(PodcastService.class);

    @Autowired
    private PodcastRepository podcastRepository;

    private PodcastMapper podcastMapper = PodcastMapper.INSTANCE;

    private TeaserMapper teaserMapper = TeaserMapper.INSTANCE;

    @Transactional
    public Optional<PodcastDTO> save(PodcastDTO podcastDTO) {
        log.debug("Request to save Podcast : {}", podcastDTO);
        final Podcast podcast = podcastMapper.map(podcastDTO);
        final Podcast result = podcastRepository.save(podcast);
        return Optional.of(podcastMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOne(Long id) {
        log.debug("Request to get Podcast (ID) : {}", id);
        final Podcast result = podcastRepository.findOne(id);
        return Optional.ofNullable(podcastMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Podcast (EXO) : {}", exo);
        final Podcast result = podcastRepository.findOneByEchoId(exo);
        return Optional.ofNullable(podcastMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOneByFeed(String feedExo) {
        log.debug("Request to get Podcast by feed (EXO) : {}", feedExo);
        final Podcast result = podcastRepository.findOneByFeed(feedExo);
        return Optional.ofNullable(podcastMapper.map(result));
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAll() {
        log.debug("Request to get all Podcasts");
        return podcastRepository.findAll().stream()
            .map(podcastMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAllAsTeaser() {
        log.debug("Request to get all Podcasts as teaser");
        return podcastRepository.findAll().stream()
            .map(teaserMapper::asTeaser)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAllRegistrationComplete(int page, int size) {
        log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size);
        final Sort sort = new Sort(new Sort.Order(Direction.ASC, "title"));
        final PageRequest pageable =  new PageRequest(page, size, sort);
        return podcastRepository.findByRegistrationCompleteTrue(pageable).stream()
            .map(podcastMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        log.debug("Request to count all Podcasts");
        return podcastRepository.countAll();
    }

    @Transactional(readOnly = true)
    public Long countAllRegistrationComplete() {
        log.debug("Request to count all Podcasts where registration is complete");
        return podcastRepository.countAllRegistrationCompleteTrue();
    }

}
