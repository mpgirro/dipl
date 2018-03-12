package echo.microservice.catalog.service;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.entity.Chapter;
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

    private ChapterMapper chapterMapper = ChapterMapper.INSTANCE;

    @Transactional
    public Optional<ChapterDTO> save(ChapterDTO chapterDTO) {
        log.debug("Request to save Chapter : {}", chapterDTO);
        final Chapter chapter = chapterMapper.map(chapterDTO);
        final Chapter result = chapterRepository.save(chapter);
        return Optional.of(chapterMapper.map(result));
    }

    @Transactional(readOnly = true)
    public void saveAll(Long episodeId, List<ChapterDTO> chapters) {
        log.debug("Request to save Chapters for Episode (ID) : {}", episodeId);
        for (ChapterDTO c : chapters) {
            c.setEpisodeId(episodeId);
            save(c);
        }
    }

    @Transactional(readOnly = true)
    public List<ChapterDTO> findAllbyEpisode(String episodeExo) {
        log.debug("Request to get all Chapters by Episode (EXO) : {}", episodeExo);
        return chapterRepository.findAllByEpisodeEchoId(episodeExo).stream()
            .map(chapterMapper::map)
            .collect(Collectors.toList());
    }

}
