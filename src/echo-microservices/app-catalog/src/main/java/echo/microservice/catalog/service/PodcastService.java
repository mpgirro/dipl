package echo.microservice.catalog.service;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.Podcast;
import echo.core.mapper.PodcastMapper;
import echo.core.mapper.TeaserMapper;
import echo.microservice.catalog.repository.PodcastRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${echo.catalog.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.catalog.default-size:20}")
    private Integer DEFAULT_SIZE;

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

    @Transactional
    public Optional<PodcastDTO> update(PodcastDTO podcastDTO) {
        log.debug("Request to update Podcast : {}", podcastDTO);
        return findOneByEchoId(podcastDTO.getEchoId())
            .map(podcast -> {
                final long id = podcast.getId();
                podcastMapper.update(podcastDTO, podcast);
                podcast.setId(id);
                return save(podcast);
            })
            .orElse(Optional.empty());
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
    public List<PodcastDTO> findAll(Integer page, Integer size) {
        log.debug("Request to get all Podcasts by page/size : ({},{})", page, size);
        final PageRequest pageable = getPageableSortedByTitle(page, size);
        return podcastRepository.findAll(pageable).getContent().stream()
            .map(podcastMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAllAsTeaser(Integer page, Integer size) {
        log.debug("Request to get all Podcasts as teaser by page/size : ({},{})", page, size);
        final PageRequest pageable = getPageableSortedByTitle(page, size);
        return podcastRepository.findAll(pageable).getContent().stream()
            .map(teaserMapper::asTeaser)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAllRegistrationComplete(Integer page, Integer size) {
        log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size);
        final PageRequest pageable = getPageableSortedByTitle(page, size);
        return podcastRepository.findByRegistrationCompleteTrue(pageable).stream()
            .map(podcastMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAllRegistrationCompleteAsTeaser(Integer page, Integer size) {
        log.debug("Request to get all Podcasts as teaser where registration is complete by page : {} and size : {}", page, size);
        final PageRequest pageable = getPageableSortedByTitle(page, size);
        return podcastRepository.findByRegistrationCompleteTrue(pageable).stream()
            .map(teaserMapper::asTeaser)
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

    private PageRequest getPageableSortedByTitle(Integer page, Integer size) {
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

        final Sort sort = new Sort(new Sort.Order(Direction.ASC, "title"));
        return new PageRequest(p, s, sort);
    }

}
