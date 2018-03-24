package echo.core.mapper;

import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.ImmutableFeedDTO;
import echo.core.domain.dto.ModifiableFeedDTO;
import echo.core.domain.entity.Feed;
import echo.core.domain.entity.Podcast;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    ModifiableFeedDTO toModifiable(Feed feed);

    default ImmutableFeedDTO toImmutable(Feed feed) {
        return toModifiable(feed).toImmutable();
    }

    @Mapping(source = "podcastId", target = "podcast")
    Feed toEntity(FeedDTO feedDto);

    ModifiableFeedDTO update(FeedDTO src, @MappingTarget ModifiableFeedDTO target);

    default ImmutableFeedDTO updateImmutable(FeedDTO src, @MappingTarget FeedDTO target) {
        return update(src, new ModifiableFeedDTO().from(target)).toImmutable();
    }

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
