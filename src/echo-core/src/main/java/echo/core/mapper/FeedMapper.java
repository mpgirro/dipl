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

import java.util.Optional;

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
        return Optional.ofNullable(feed)
            .map(f -> toModifiable(f).toImmutable())
            .orElse(null);
    }

    default ImmutableFeedDTO toImmutable(FeedDTO feed) {

        if (feed == null) return null;

        if (feed instanceof  ImmutableFeedDTO) {
            return (ImmutableFeedDTO) feed;
        }
        return ((ModifiableFeedDTO) feed).toImmutable();
    }

    @Mapping(source = "podcastId", target = "podcast")
    Feed toEntity(FeedDTO feedDto);

    ModifiableFeedDTO update(FeedDTO src, @MappingTarget ModifiableFeedDTO target);

    default ModifiableFeedDTO update(FeedDTO src, @MappingTarget FeedDTO target) {

        if (target == null) return null;

        ModifiableFeedDTO modTarget;
        if (target instanceof  ModifiableFeedDTO) {
            modTarget = (ModifiableFeedDTO) target;
        } else {
            modTarget = new ModifiableFeedDTO().from(target);
        }
        return update(src, modTarget);
    }

    default ImmutableFeedDTO updateImmutable(FeedDTO src, @MappingTarget FeedDTO target) {

        if (target == null) return null;

        ModifiableFeedDTO modTarget;
        if (target instanceof  ModifiableFeedDTO) {
            modTarget = (ModifiableFeedDTO) target;
        } else {
            modTarget = new ModifiableFeedDTO().from(target);
        }
        return update(src, modTarget).toImmutable();
    }

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
