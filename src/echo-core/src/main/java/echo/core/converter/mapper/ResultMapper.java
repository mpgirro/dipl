package echo.core.converter.mapper;

import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.IndexResult;
import echo.core.model.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper
public interface ResultMapper {

    ResultMapper INSTANCE = Mappers.getMapper( ResultMapper.class );

    @Mappings( {
        @Mapping(target = "docType", constant = "podcast"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "itunesImage", target = "itunesImage")
    } )
    IndexResult podcastDtoToIndexResult(PodcastDTO podcast);

    List<IndexResult> podcastDtosToIndexResults(List<PodcastDTO> podcastDtos);

    @Mappings( {
        @Mapping(target = "docType", constant = "episode"),
        @Mapping(source = "echoId", target = "echoId"),
        @Mapping(source = "title", target = "title"),
        @Mapping(source = "link", target = "link"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "pubDate", target = "pubDate"),
        @Mapping(source = "itunesImage", target = "itunesImage")
    } )
    IndexResult episodeDtoToIndexResult(EpisodeDTO episodeDTO);

    List<IndexResult> episodeDtosToIndexResults(List<EpisodeDTO> episodeDtos);

}
