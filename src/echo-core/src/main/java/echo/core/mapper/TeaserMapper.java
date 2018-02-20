package echo.core.mapper;

import echo.core.model.domain.Episode;
import echo.core.model.domain.Podcast;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses={UrlMapper.class, DateMapper.class})
public interface TeaserMapper {

    TeaserMapper INSTANCE = Mappers.getMapper( TeaserMapper.class );

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "itunesImage")
    PodcastDTO asTeaser(PodcastDTO dto);

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "itunesImage")
    PodcastDTO asTeaser(Podcast podcast);

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "pubDate")
    @Mapping(target = "description")
    @Mapping(target = "itunesImage")
    @Mapping(target = "itunesDuration")
    EpisodeDTO asTeaser(EpisodeDTO dto);

    @Mapping(target = "echoId")
    @Mapping(target = "title")
    @Mapping(target = "pubDate")
    @Mapping(target = "description")
    @Mapping(target = "itunesImage")
    @Mapping(target = "itunesDuration")
    EpisodeDTO asTeaser(Episode episode);

}
