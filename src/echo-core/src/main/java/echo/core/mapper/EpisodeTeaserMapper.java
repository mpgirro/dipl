package echo.core.mapper;

import echo.core.model.dto.EpisodeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses={PodcastMapper.class, DateMapper.class})
public interface EpisodeTeaserMapper {

    EpisodeTeaserMapper INSTANCE = Mappers.getMapper( EpisodeTeaserMapper.class );

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "pubDate")
    @Mapping(target = "description")
    @Mapping(target = "itunesImage")
    @Mapping(target = "itunesDuration")
    EpisodeDTO map(EpisodeDTO dto);

}
