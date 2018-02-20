package echo.core.mapper;

import echo.core.model.dto.DTO;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.IndexDocDTO;
import echo.core.model.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper
public interface IndexDocMapper {

    IndexDocMapper INSTANCE = Mappers.getMapper( IndexDocMapper.class );

    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "contentEncoded", ignore = true)
    IndexDocDTO podcastDtoToIndexResult(PodcastDTO podcast);

    List<IndexDocDTO> podcastDtosToIndexResults(List<PodcastDTO> podcastDtos);

    @Mapping(target = "docType", constant = "episode")
    IndexDocDTO episodeDtoToIndexResult(EpisodeDTO episodeDTO);

    List<IndexDocDTO> episodeDtosToIndexResults(List<EpisodeDTO> episodeDtos);

    default IndexDocDTO dtoToIndexDoc(DTO dto) {
        final IndexDocDTO doc;
        if (dto instanceof PodcastDTO) {
            doc = IndexDocMapper.INSTANCE.podcastDtoToIndexResult((PodcastDTO) dto);
        } else if (dto instanceof EpisodeDTO) {
            doc = IndexDocMapper.INSTANCE.episodeDtoToIndexResult((EpisodeDTO) dto);
        } else {
            throw new RuntimeException("Unsupported DTO type : " + dto.getClass());
        }
        return doc;
    }

}
