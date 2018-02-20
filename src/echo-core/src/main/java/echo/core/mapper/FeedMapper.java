package echo.core.mapper;

import echo.core.model.domain.Feed;
import echo.core.model.domain.Podcast;
import echo.core.model.dto.FeedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, DateMapper.class})
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    FeedDTO feedToFeedDto(Feed feed);

    List<FeedDTO> feedsToFeedDtos(List<Feed> feeds);

    List<FeedDTO> feedsToFeedDtos(Set<Feed> feeds);

    @Mapping(source = "podcastId", target = "podcast")
    Feed feedDtoToFeed(FeedDTO feedDto);

    List<Feed> feedDtosToFeeds(List<FeedDTO> feedDtos);

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
