package echo.core.mapper;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * This mapper cleans all database IDs by setting them to null. Database IDs are only
 * valid in a local scope, and without ensuring they are whiped before sending a DTO
 * to a remote scope could cause to confusion and invalid databases.
 *
 * Identification in a global scope is done by using the EXO (= external ID) value,
 * which this mapper leaves untouched.
 *
 * @author Maximilian Irro
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface NullMapper {

    NullMapper INSTANCE = Mappers.getMapper( NullMapper.class );

    @Mapping(target = "id", ignore = true)
    PodcastDTO map(PodcastDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    EpisodeDTO map(EpisodeDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    FeedDTO map(FeedDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "episodeId", ignore = true)
    ChapterDTO map(ChapterDTO dto);

}
