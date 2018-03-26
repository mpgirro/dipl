package echo.core.mapper;

import echo.core.domain.dto.*;
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
    ModifiablePodcastDTO clearModifiable(PodcastDTO dto);

    default ImmutablePodcastDTO clearImmutable(PodcastDTO dto) {
        return (dto == null) ? null : clearModifiable(dto).toImmutable();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableEpisodeDTO clearModifiable(EpisodeDTO dto);

    default ImmutableEpisodeDTO clearImmutable(EpisodeDTO dto) {
        return (dto == null) ? null : clearModifiable(dto).toImmutable();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "podcastId", ignore = true)
    ModifiableFeedDTO clearModifiable(FeedDTO dto);

    default ImmutableFeedDTO clearImmutable(FeedDTO dto) {
        return (dto == null) ? null : clearModifiable(dto).toImmutable();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "episodeId", ignore = true)
    ModifiableChapterDTO clearModifiable(ChapterDTO dto);

    default ImmutableChapterDTO clearImmutable(ChapterDTO dto) {
        return (dto == null) ? null : clearModifiable(dto).toImmutable();
    }

}
