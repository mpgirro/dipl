package echo.core.mapper;

import echo.core.domain.dto.immutable.ModifiableTestFeed;
import echo.core.domain.dto.immutable.TestFeed;
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
@Mapper(uses={TestPodcastMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TestFeedMapper {

    TestFeedMapper INSTANCE = Mappers.getMapper( TestFeedMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    ModifiableTestFeed map(Feed feed);

    @Mapping(source = "podcastId", target = "podcast")
    Feed map(TestFeed feedDto);

    ModifiableTestFeed update(TestFeed src, @MappingTarget ModifiableTestFeed target);

    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

}
