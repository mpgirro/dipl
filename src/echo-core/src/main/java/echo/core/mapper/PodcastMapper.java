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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        return Optional.ofNullable(podcast)
            .map(p -> toModifiable(p).toImmutable())
            .orElse(null);
    }

    default ImmutablePodcastDTO toImmutable(PodcastDTO podcast) {

        if (podcast == null) return null;

        if (podcast instanceof  ImmutablePodcastDTO) {
            return (ImmutablePodcastDTO) podcast;
        }
        return ((ModifiablePodcastDTO) podcast).toImmutable();
    }

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast toEntity(PodcastDTO dto);

    ModifiablePodcastDTO update(PodcastDTO src, @MappingTarget ModifiablePodcastDTO target);

    // TODO
    default ModifiablePodcastDTO update(PodcastDTO src, @MappingTarget PodcastDTO target) {

        if (target == null) return null;

        ModifiablePodcastDTO modTarget;
        if (target instanceof  ModifiablePodcastDTO) {
            modTarget = (ModifiablePodcastDTO) target;
        } else {
            modTarget = new ModifiablePodcastDTO().from(target);
        }
        return update(src, modTarget);
    }

    default ImmutablePodcastDTO updateImmutable(PodcastDTO src, @MappingTarget PodcastDTO target) {

        if (target == null) return null;

        ModifiablePodcastDTO modTarget;
        if (target instanceof  ModifiablePodcastDTO) {
            modTarget = (ModifiablePodcastDTO) target;
        } else {
            modTarget = new ModifiablePodcastDTO().from(target);
        }
        return update(src, modTarget).toImmutable();
    }

    default ImmutablePodcastDTO toImmutable(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final ImmutablePodcastDTO.Builder builder = ImmutablePodcastDTO.builder();

        Optional.ofNullable(doc.get(IndexField.ECHO_ID))
            .ifPresent(builder::setEchoId);
        Optional.ofNullable(doc.get(IndexField.TITLE))
            .ifPresent(builder::setTitle);
        Optional.ofNullable(doc.get(IndexField.LINK))
            .ifPresent(builder::setLink);
        Optional.ofNullable(doc.get(IndexField.PUB_DATE))
            .map(DateMapper.INSTANCE::asLocalDateTime)
            .ifPresent(builder::setPubDate);
        Optional.ofNullable(Stream.of(doc.get(IndexField.ITUNES_SUMMARY), doc.get(IndexField.DESCRIPTION))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null))
            .ifPresent(builder::setDescription);
        Optional.ofNullable(doc.get(IndexField.ITUNES_IMAGE))
            .ifPresent(builder::setImage);

        /*
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
        */

        return builder.create();
    }

}
