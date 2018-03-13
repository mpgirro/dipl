package echo.microservice.catalog.service;

import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.PodcastMapper;
import echo.core.mapper.TeaserMapper;
import echo.microservice.catalog.repository.EpisodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EpisodeService {

    private final Logger log = LoggerFactory.getLogger(EpisodeService.class);

    @Autowired
    private EpisodeRepository episodeRepository;

    private PodcastMapper podcastMapper = PodcastMapper.INSTANCE;
    private EpisodeMapper episodeMapper = EpisodeMapper.INSTANCE;
    private TeaserMapper teaserMapper = TeaserMapper.INSTANCE;

    @Transactional
    public Optional<EpisodeDTO> save(EpisodeDTO episodeDTO) {
        log.debug("Request to save Episode : {}", episodeDTO);
        final Episode episode = episodeMapper.map(episodeDTO);
        final Episode result = episodeRepository.save(episode);
        return Optional.of(episodeMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOne(Long id) {
        log.debug("Request to get Episode (ID) : {}", id);
        final Episode result = episodeRepository.findOne(id);
        return Optional.ofNullable(episodeMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Optional<EpisodeDTO> findOneByEchoId(String exo) {
        log.debug("Request to get Episode (EXO) : {}", exo);
        final Episode result = episodeRepository.findOneByEchoId(exo);
        return Optional.ofNullable(episodeMapper.map(result));
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAll() {
        log.debug("Request to get all Episodes");
        return episodeRepository.findAll().stream()
            .map(episodeMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcast(PodcastDTO podcastDTO) {
        log.debug("Request to get all Episodes by Podcast : ", podcastDTO);
        final Podcast podcast = podcastMapper.map(podcastDTO);
        return episodeRepository.findAllByPodcast(podcast).stream()
            .map(episodeMapper::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeDTO> findAllByPodcast(String podcastExo) {
        log.debug("Request to get all Episodes by Podcast (EXO) : ", podcastExo);
        return episodeRepository.findAllByPodcastEchoId(podcastExo).stream()
            .map(episodeMapper::map)
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
        final Episode result = episodeRepository.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType);
        return Optional.ofNullable(episodeMapper.map(result));
    }

    @Transactional(readOnly = true)
    public Long countAll() {
        log.debug("Request to count all Episodes");
        return episodeRepository.countAll();
    }

}
