package echo.core.converter.mapper;

import echo.core.model.domain.Feed;
import echo.core.model.dto.FeedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={DateMapper.class})
public interface FeedMapper {

    FeedMapper INSTANCE = Mappers.getMapper( FeedMapper.class );

    @Mappings( {
        @Mapping(source = "url", target = "url"),
        @Mapping(source = "lastChecked", target = "lastChecked"),
        @Mapping(source = "lastStatus", target = "lastStatus")
    } )
    FeedDTO feedToFeedDto(Feed feed);

    List<FeedDTO> feedsToFeedDtos(List<Feed> feeds);

    @Mappings( {
        @Mapping(source = "url", target = "url"),
        @Mapping(source = "lastChecked", target = "lastChecked"),
        @Mapping(source = "lastStatus", target = "lastStatus")
    } )
    Feed feedDtoToFeed(FeedDTO feedDto);

    List<Feed> feedDtosToFeeds(List<FeedDTO> feedDtos);

}
