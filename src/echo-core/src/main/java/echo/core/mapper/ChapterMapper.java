package echo.core.mapper;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.ImmutableChapterDTO;
import echo.core.domain.dto.ModifiableChapterDTO;
import echo.core.domain.entity.Chapter;
import echo.core.domain.entity.Episode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={EpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ChapterMapper {

    ChapterMapper INSTANCE = Mappers.getMapper( ChapterMapper.class );

    @Mapping(source = "episode.id", target = "episodeId")
    @Mapping(source = "episode.echoId", target = "episodeExo")
    ModifiableChapterDTO toModifiable(Chapter chapter);

    @Mapping(source = "episodeId", target = "episode")
    Chapter toEntity(ChapterDTO dto);

    ModifiableChapterDTO update(ChapterDTO src, @MappingTarget ModifiableChapterDTO target);

    default ImmutableChapterDTO updateImmutable(ChapterDTO src, @MappingTarget ChapterDTO target) {
        return update(src, new ModifiableChapterDTO().from(target)).toImmutable();
    }

    default Episode episodeFromId(Long id) {

        if (id == null) return null;

        final Episode episode = new Episode();
        episode.setId(id);

        return episode;
    }

}
