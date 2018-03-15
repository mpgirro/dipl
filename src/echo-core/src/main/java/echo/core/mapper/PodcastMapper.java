package echo.core.mapper;

import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.Podcast;
import echo.core.index.IndexField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={UrlMapper.class, DateMapper.class})
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    PodcastDTO map(Podcast podcast);

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast map(PodcastDTO podcastDto);

    PodcastDTO update(PodcastDTO src, @MappingTarget PodcastDTO target);

    default PodcastDTO map(org.apache.lucene.document.Document doc){

        if (doc == null) return null;

        final PodcastDTO dto = new PodcastDTO();

        if (doc.get(IndexField.ECHO_ID)      != null) { dto.setEchoId(doc.get(IndexField.ECHO_ID)); }
        if (doc.get(IndexField.TITLE)        != null) { dto.setTitle(doc.get(IndexField.TITLE)); }
        if (doc.get(IndexField.LINK)         != null) { dto.setLink(doc.get(IndexField.LINK)); }
        if (doc.get(IndexField.PUB_DATE)     != null) { dto.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get(IndexField.PUB_DATE))); }
        if (doc.get(IndexField.ITUNES_SUMMARY) != null) {
            dto.setDescription(doc.get(IndexField.ITUNES_SUMMARY));
        } else if (doc.get(IndexField.DESCRIPTION) != null) {
            dto.setDescription(doc.get(IndexField.DESCRIPTION));
        }
        if (doc.get(IndexField.ITUNES_IMAGE) != null) { dto.setImage(doc.get(IndexField.ITUNES_IMAGE)); }

        return dto;
    }

}
