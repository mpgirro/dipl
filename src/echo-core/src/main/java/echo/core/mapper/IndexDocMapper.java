package echo.core.mapper;

import echo.core.domain.dto.DTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper
public interface IndexDocMapper {

    IndexDocMapper INSTANCE = Mappers.getMapper( IndexDocMapper.class );

    @Mapping(target = "docType", constant = "podcast")
    @Mapping(target = "contentEncoded", ignore = true)
    IndexDocDTO map(PodcastDTO podcast);

    @Mapping(target = "docType", constant = "episode")
    IndexDocDTO map(EpisodeDTO episodeDTO);

    default IndexDocDTO dtoToIndexDoc(DTO dto) {
        final IndexDocDTO doc;
        if (dto instanceof PodcastDTO) {
            doc = IndexDocMapper.INSTANCE.map((PodcastDTO) dto);
        } else if (dto instanceof EpisodeDTO) {
            doc = IndexDocMapper.INSTANCE.map((EpisodeDTO) dto);
        } else {
            throw new RuntimeException("Unsupported DTO type : " + dto.getClass());
        }
        return doc;
    }

}
