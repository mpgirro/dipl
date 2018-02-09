package echo.core.mapper;

import echo.core.model.domain.Feed;
import echo.core.model.domain.Podcast;
import echo.core.model.dto.FeedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, DateMapper.class})
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "podcast.id", target = "podcastId"),
        @Mapping(source = "url", target = "url"),
        @Mapping(source = "lastChecked", target = "lastChecked"),
        @Mapping(source = "lastStatus", target = "lastStatus")
    } )
    FeedDTO feedToFeedDto(Feed feed);

    List<FeedDTO> feedsToFeedDtos(List<Feed> feeds);

    List<FeedDTO> feedsToFeedDtos(Set<Feed> feeds);

    @Mappings( {
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "podcastId", target = "podcast"),
        @Mapping(source = "url", target = "url"),
        @Mapping(source = "lastChecked", target = "lastChecked"),
        @Mapping(source = "lastStatus", target = "lastStatus")
    } )
    Feed feedDtoToFeed(FeedDTO feedDto);

    List<Feed> feedDtosToFeeds(List<FeedDTO> feedDtos);

    default Podcast podcastFromId(Long id) {
        if (id == null) {
            return null;
        }
        Podcast podcast = new Podcast();
        podcast.setId(id);
        return podcast;
    }

}
