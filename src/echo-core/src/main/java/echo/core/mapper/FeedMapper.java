package echo.core.mapper;

import echo.core.domain.entity.Feed;
import echo.core.domain.entity.Podcast;
import echo.core.domain.dto.FeedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, DateMapper.class})
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    FeedDTO map(Feed feed);

    @Mapping(source = "podcastId", target = "podcast")
    Feed map(FeedDTO feedDto);

    FeedDTO update(FeedDTO src, @MappingTarget FeedDTO target);

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
