package echo.core.mapper;

import echo.core.domain.dto.ImmutablePodcastDTO;
import echo.core.domain.dto.ModifiablePodcastDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.entity.Podcast;
import echo.core.index.IndexField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(
    uses={UrlMapper.class, DateMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    ModifiablePodcastDTO toModifiable(Podcast podcast);

    default ImmutablePodcastDTO toImmutable(Podcast podcast) {
        return toModifiable(podcast).toImmutable();
    }

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast toEntity(PodcastDTO dto);

    ModifiablePodcastDTO update(PodcastDTO src, @MappingTarget ModifiablePodcastDTO target);

    default ImmutablePodcastDTO updateImmutable(PodcastDTO src, @MappingTarget PodcastDTO target) {
        return update(src, new ModifiablePodcastDTO().from(target)).toImmutable();
    }

    default ImmutablePodcastDTO toImmutable(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final ImmutablePodcastDTO.Builder builder = ImmutablePodcastDTO.builder();

        if (doc.get(IndexField.ECHO_ID)        != null) { builder.setEchoId(doc.get(IndexField.ECHO_ID)); }
        if (doc.get(IndexField.TITLE)          != null) { builder.setTitle(doc.get(IndexField.TITLE)); }
        if (doc.get(IndexField.LINK)           != null) { builder.setLink(doc.get(IndexField.LINK)); }
        if (doc.get(IndexField.PUB_DATE)       != null) { builder.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get(IndexField.PUB_DATE))); }
        if (doc.get(IndexField.ITUNES_SUMMARY) != null) {
            builder.setDescription(doc.get(IndexField.ITUNES_SUMMARY));
        } else if (doc.get(IndexField.DESCRIPTION) != null) {
            builder.setDescription(doc.get(IndexField.DESCRIPTION));
        }
        if (doc.get(IndexField.ITUNES_IMAGE) != null) { builder.setImage(doc.get(IndexField.ITUNES_IMAGE)); }

        return builder.create();
    }

}
