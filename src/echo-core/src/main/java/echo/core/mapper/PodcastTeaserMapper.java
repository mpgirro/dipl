package echo.core.mapper;

import echo.core.model.domain.Podcast;
import echo.core.model.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses={UrlMapper.class, DateMapper.class})
public interface PodcastTeaserMapper {

    PodcastTeaserMapper INSTANCE = Mappers.getMapper( PodcastTeaserMapper.class );

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "itunesImage")
    PodcastDTO asTeaser(PodcastDTO dto);

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "itunesImage")
    PodcastDTO asTeaser(Podcast podcast);
}
