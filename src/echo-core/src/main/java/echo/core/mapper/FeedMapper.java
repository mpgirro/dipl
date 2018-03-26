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
@Mapper(uses = {PodcastMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    ModifiableFeedDTO toModifiable(Feed feed);

    default ImmutableFeedDTO toImmutable(Feed feed) {
        return Optional.ofNullable(feed)
            .map(this::toModifiable)
            .map(ModifiableFeedDTO::toImmutable)
            .orElse(null);
    }

    default ModifiableFeedDTO toModifiable(FeedDTO feed) {
        return Optional.ofNullable(feed)
            .map(f -> {
                if (f instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) f;
                } else {
                    return new ModifiableFeedDTO().from(f);
                }
            })
            .orElse(null);
    }

    default ImmutableFeedDTO toImmutable(FeedDTO feed) {
        return Optional.ofNullable(feed)
            .map(f -> {
                if (f instanceof ImmutableFeedDTO) {
                    return (ImmutableFeedDTO) f;
                } else {
                    return ((ModifiableFeedDTO) f).toImmutable();
                }
            })
            .orElse(null);
    }

    @Mapping(source = "podcastId", target = "podcast")
    Feed toEntity(FeedDTO feedDto);

    ModifiableFeedDTO update(FeedDTO src, @MappingTarget ModifiableFeedDTO target);

    default ModifiableFeedDTO update(FeedDTO src, @MappingTarget FeedDTO target) {
        return Optional.ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) t;
                } else {
                    return new ModifiableFeedDTO().from(t);
                }
            })
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutableFeedDTO updateImmutable(FeedDTO src, @MappingTarget FeedDTO target) {
        return Optional.ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiableFeedDTO) {
                    return (ModifiableFeedDTO) t;
                } else {
                    return new ModifiableFeedDTO().from(t);
                }
            })
            .map(t -> update(src, t))
            .map(ModifiableFeedDTO::toImmutable)
            .orElse(null);
    }

    default Podcast podcastFromId(Long podcastId) {
        return Optional.ofNullable(podcastId)
            .map(id -> {
                final Podcast p = new Podcast();
                p.setId(id);
                return p;
            })
            .orElse(null);
    }

}
