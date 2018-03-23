package echo.core.mapper;

import echo.core.domain.dto.FeedDTO;
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
    ModifiableFeedDTO map(Feed feed);

    @Mapping(source = "podcastId", target = "podcast")
    Feed map(FeedDTO feedDto);

    ModifiableFeedDTO update(FeedDTO src, @MappingTarget ModifiableFeedDTO target);

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
