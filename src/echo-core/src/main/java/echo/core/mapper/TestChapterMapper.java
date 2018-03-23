package echo.core.mapper;

import echo.core.domain.dto.immutable.ModifiableTestChapter;
import echo.core.domain.dto.immutable.TestChapter;
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
@Mapper(uses={TestEpisodeMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TestChapterMapper {

    TestChapterMapper INSTANCE = Mappers.getMapper( TestChapterMapper.class );

    @Mapping(source = "episode.id", target = "episodeId")
    @Mapping(source = "episode.echoId", target = "episodeExo")
    ModifiableTestChapter map(Chapter entity);

    @Mapping(source = "episodeId", target = "episode")
    Chapter map(TestChapter dto);

    ModifiableTestChapter update(TestChapter src, @MappingTarget ModifiableTestChapter target);

    default Episode episodeFromId(Long id) {

        if (id == null) return null;

        final Episode episode = new Episode();
        episode.setId(id);

        return episode;
    }

}
