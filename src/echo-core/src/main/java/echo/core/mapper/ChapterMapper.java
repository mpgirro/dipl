package echo.core.mapper;

import echo.core.domain.entity.Chapter;
import echo.core.domain.entity.Episode;
import echo.core.domain.dto.ChapterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={EpisodeMapper.class})
public interface ChapterMapper {

    ChapterMapper INSTANCE = Mappers.getMapper( ChapterMapper.class );

    @Mapping(source = "episode.id", target = "episodeId")
    ChapterDTO map(Chapter entity);

    @Mapping(source = "episodeId", target = "episode")
    Chapter map(ChapterDTO dto);

    ChapterDTO update(ChapterDTO src, @MappingTarget ChapterDTO target);

    default Episode episodeFromId(Long id) {

        if (id == null) return null;

        final Episode episode = new Episode();
        episode.setId(id);

        return episode;
    }
}
