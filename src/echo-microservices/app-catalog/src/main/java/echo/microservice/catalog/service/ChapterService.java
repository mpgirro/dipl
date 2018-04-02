package echo.microservice.catalog.service;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.ImmutableChapterDTO;
import echo.core.domain.dto.ModifiableChapterDTO;
import echo.core.domain.entity.ChapterEntity;
import echo.core.mapper.ChapterMapper;
import echo.microservice.catalog.repository.ChapterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ChapterService {

    private final Logger log = LoggerFactory.getLogger(ChapterService.class);

    @Autowired
    private ChapterRepository chapterRepository;

    private final ChapterMapper chapterMapper = ChapterMapper.INSTANCE;

    @Transactional
    public Optional<ChapterDTO> save(ChapterDTO chapter) {
        log.debug("Request to save Chapter : {}", chapter);
        return Optional.of(chapter)
            .map(chapterMapper::toModifiable)
            .map(chapterMapper::toEntity)
            .map(chapterRepository::save)
            .map(chapterMapper::toModifiable)
            .map(ModifiableChapterDTO::toImmutable);
    }

    @Transactional
    public void saveAll(Long episodeId, List<ChapterDTO> chapters) {
        log.debug("Request to save Chapters for Episode (ID) : {}", episodeId);
        chapters.stream()
            .map(chapterMapper::toModifiable)
            .map(c -> c.setEpisodeId(episodeId))
            .forEach(this::save);
    }

    @Transactional(readOnly = true)
    public List<ChapterDTO> findAllByEpisode(String episodeExo) {
        log.debug("Request to get all Chapters by Episode (EXO) : {}", episodeExo);
        return chapterRepository.findAllByEpisodeExo(episodeExo).stream()
            .map(chapterMapper::toModifiable)
            .map(ModifiableChapterDTO::toImmutable)
            .collect(Collectors.toList());
    }

}
