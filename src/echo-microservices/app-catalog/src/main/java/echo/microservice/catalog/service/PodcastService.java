package echo.microservice.catalog.service;

import com.google.common.base.MoreObjects;
import echo.core.domain.dto.PodcastDTO;
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

/**
 * @author Maximilian Irro
 */
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

    private final PodcastMapper podcastMapper = PodcastMapper.INSTANCE;
    private final TeaserMapper teaserMapper = TeaserMapper.INSTANCE;

    @Transactional
    public Optional<PodcastDTO> save(PodcastDTO podcastDTO) {
        log.debug("Request to save Podcast : {}", podcastDTO);
        return Optional.of(podcastDTO)
            .map(podcastMapper::toModifiable)
            .map(podcastMapper::toEntity)
            .map(podcastRepository::save)
            .map(podcastMapper::toImmutable);
    }

    @Transactional
    public Optional<PodcastDTO> update(PodcastDTO podcastDTO) {
        log.debug("Request to update Podcast : {}", podcastDTO);
        return findOneByEchoId(podcastDTO.getEchoId())
            .map(podcastMapper::toModifiable)
            .map(p -> {
                final Long id = p.getId();
                podcastMapper.update(podcastDTO, p);
                p.setId(id);
                return save(p);
            })
            .orElse(Optional.empty());
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOne(Long id) {
        log.debug("Request to get Podcast (ID) : {}", id);
        return Optional
            .ofNullable(podcastRepository.findOne(id))
            .map(podcastMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Podcast (EXO) : {}", exo);
        return Optional
            .ofNullable(podcastRepository.findOneByEchoId(exo))
            .map(podcastMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public Optional<PodcastDTO> findOneByFeed(String feedExo) {
        log.debug("Request to get Podcast by feed (EXO) : {}", feedExo);
        return Optional
            .ofNullable(podcastRepository.findOneByFeed(feedExo))
            .map(podcastMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public List<PodcastDTO> findAll(Integer page, Integer size) {
        log.debug("Request to get all Podcasts by page/size : ({},{})", page, size);
        final PageRequest pageable = getPageableSortedByTitle(page, size);
        return podcastRepository.findAll(pageable).getContent().stream()
            .map(podcastMapper::toImmutable)
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
            .map(podcastMapper::toImmutable)
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
