package echo.microservice.catalog.service;

import echo.core.async.catalog.RegisterEpisodeIfNewJobCatalogJob;
import echo.core.async.index.AddOrUpdateDocIndexJob;
import echo.core.async.index.ImmutableAddOrUpdateDocIndexJob;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.ModifiableEpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.PodcastEntity;
import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.IndexMapper;
import echo.core.mapper.PodcastMapper;
import echo.core.mapper.TeaserMapper;
import echo.core.util.ExoGenerator;
import echo.microservice.catalog.ExoUtil;
import echo.microservice.catalog.async.IndexQueueSender;
import echo.microservice.catalog.repository.EpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class EpisodeService {

    private final Logger log = LoggerFactory.getLogger(EpisodeService.class);

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private PodcastService podcastService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private IndexQueueSender indexQueueSender;

    private final PodcastMapper podcastMapper = PodcastMapper.INSTANCE;
    private final EpisodeMapper episodeMapper = EpisodeMapper.INSTANCE;
    private final TeaserMapper teaserMapper = TeaserMapper.INSTANCE;
    private final IndexMapper indexMapper = IndexMapper.INSTANCE;

    @Transactional
    public Optional<EpisodeDTO> save(EpisodeDTO episodeDTO) {
        log.debug("Request to save Episode : {}", episodeDTO);
        return Optional.of(episodeDTO)
            .map(episodeMapper::toModifiable)
            .map(episodeMapper::toEntity)
            .map(episodeRepository::save)
            .map(episodeMapper::toImmutable);
    }

    @Async
    @Transactional
    public void register(RegisterEpisodeIfNewJobCatalogJob job) {
        log.debug("Request to register Podcast(EXO)/Episode : ({},{})", job.getPodcastExo(), job.getEpisode());

        final String podcastExo = job.getPodcastExo();
        final ModifiableEpisodeDTO e = new ModifiableEpisodeDTO().from(job.getEpisode());

        boolean found;
        if (!isNullOrEmpty(e.getGuid())) {
            found = episodeRepository.findAllByPodcastAndGuid(podcastExo, e.getGuid()).size() > 0;
        } else {
            found = Optional.ofNullable(episodeRepository
                .findOneByEnlosure(e.getEnclosureUrl(), e.getEnclosureLength(), e.getEnclosureType()))
                .isPresent();
        }

        if (!found) {

            final Optional<PodcastDTO> podcast = podcastService.findOneByEchoId(podcastExo);
            if (podcast.isPresent()) {
                final PodcastDTO p = podcast.get();
                e.setPodcastId(p.getId());
                e.setPodcastTitle(p.getTitle());

                if (isNullOrEmpty(e.getEchoId())) {
                    final String exo = ExoUtil.getInstance().getExoGenerator().getNewExo();
                    e.setEchoId(exo);
                }

                if (isNullOrEmpty(e.getImage())) {
                    e.setImage(p.getImage());
                }
            } else {
                log.error("No Podcast found with echoId : {}", podcastExo);
            }

            e.setRegistrationTimestamp(LocalDateTime.now());
            final Optional<EpisodeDTO> registered = save(e);

            registered
                .map(episodeMapper::toModifiable)
                .ifPresent(r -> {
                    // we must register the episodes chapters as well
                    Optional.ofNullable(e.getChapters()).ifPresent(cs -> chapterService.saveAll(r.getId(), cs));

                    // TODO why is this really necessary here?
                    // we'll need this info when we send the episode to the index in just a moment
                    r.setPodcastTitle(e.getPodcastTitle());

                    log.info("episode registered : '{}' [p:{},e:{}]", r.getTitle(), podcastExo, r.getEchoId());

                    final AddOrUpdateDocIndexJob indexJob = ImmutableAddOrUpdateDocIndexJob.of(indexMapper.toImmutable(r));
                    indexQueueSender.produceMsg(indexJob);

                    // TODO download episode website
                });

        } else {
            log.debug("Episode is already registered : ('{}', {}, '{}')", e.getEnclosureUrl(), e.getEnclosureLength(), e.getEnclosureType());
        }
    }

    @Transactional
    public Optional<EpisodeDTO> update(EpisodeDTO episodeDTO) {
        log.debug("Request to update Episode : {}", episodeDTO);
        return findOneByEchoId(episodeDTO.getEchoId())
            .map(episodeMapper::toModifiable)
            .map(e -> {
                final Long id = e.getId();
                episodeMapper.update(episodeDTO, e);
                e.setId(id);
                return save(e);
            })
            .orElse(Optional.empty());
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOne(Long id) {
        log.debug("Request to get Episode (ID) : {}", id);
        return Optional
            .ofNullable(episodeRepository.findOne(id))
            .map(episodeMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Episode (EXO) : {}", exo);
        return Optional
            .ofNullable(episodeRepository.findOneByEchoId(exo))
            .map(episodeMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAll() {
        log.debug("Request to get all Episodes");
        return episodeRepository.findAll().stream()
            .map(episodeMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcast(PodcastDTO podcastDTO) {
        log.debug("Request to get all Episodes by Podcast : ", podcastDTO);
        final PodcastEntity podcast = podcastMapper.toEntity(podcastDTO);
        return episodeRepository.findAllByPodcast(podcast).stream()
            .map(episodeMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcast(String podcastExo) {
        log.debug("Request to get all Episodes by Podcast (EXO) : ", podcastExo);
        return episodeRepository.findAllByPodcastEchoId(podcastExo).stream()
            .map(episodeMapper::toImmutable)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcastAsTeaser(String podcastExo) {
        log.debug("Request to get all Episodes by Podcast (EXO) as teaser : ", podcastExo);
        return episodeRepository.findAllByPodcastEchoId(podcastExo).stream()
            .map(teaserMapper::asTeaser)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcastAndGuid(String podcastExo, String guid) {
        log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid);
        return episodeRepository.findAllByPodcastAndGuid(podcastExo, guid).stream()
            .map(teaserMapper::asTeaser)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOneByEnclosure(String enclosureUrl, Long enclosureLength, String enclosureType) {
        log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType);
        return Optional
            .ofNullable(episodeRepository.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType))
            .map(episodeMapper::toImmutable);
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        log.debug("Request to count all Episodes");
        return episodeRepository.countAll();
    }

}
