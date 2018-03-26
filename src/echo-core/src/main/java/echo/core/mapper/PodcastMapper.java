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
    uses = {UrlMapper.class, DateMapper.class},
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    ModifiablePodcastDTO toModifiable(Podcast podcast);

    default ImmutablePodcastDTO toImmutable(Podcast podcast) {
        return Optional.ofNullable(podcast)
            .map(this::toModifiable)
            .map(ModifiablePodcastDTO::toImmutable)
            .orElse(null);
    }

    default ModifiablePodcastDTO toModifiable(PodcastDTO podcast) {
        return Optional.ofNullable(podcast)
            .map(p -> {
                if (p instanceof ModifiablePodcastDTO) {
                    return (ModifiablePodcastDTO) p;
                } else {
                    return new ModifiablePodcastDTO().from(p);
                }
            })
            .orElse(null);
    }

    default ImmutablePodcastDTO toImmutable(PodcastDTO podcast) {
        return Optional.ofNullable(podcast)
            .map(p -> {
                if (p instanceof ImmutablePodcastDTO) {
                    return (ImmutablePodcastDTO) p;
                } else {
                    return ((ModifiablePodcastDTO) p).toImmutable();
                }
            })
            .orElse(null);
    }

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast toEntity(PodcastDTO dto);

    ModifiablePodcastDTO update(PodcastDTO src, @MappingTarget ModifiablePodcastDTO target);

    default ModifiablePodcastDTO update(PodcastDTO src, @MappingTarget PodcastDTO target) {
        return Optional.ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiablePodcastDTO) {
                    return (ModifiablePodcastDTO) t;
                } else {
                    return new ModifiablePodcastDTO().from(t);
                }
            })
            .map(t -> update(src, t))
            .orElse(null);
    }

    default ImmutablePodcastDTO updateImmutable(PodcastDTO src, @MappingTarget PodcastDTO target) {
        return Optional.ofNullable(target)
            .map(t -> {
                if (t instanceof ModifiablePodcastDTO) {
                    return (ModifiablePodcastDTO) t;
                } else {
                    return new ModifiablePodcastDTO().from(t);
                }
            })
            .map(t -> update(src, t))
            .map(ModifiablePodcastDTO::toImmutable)
            .orElse(null);
    }

    default ImmutablePodcastDTO toImmutable(org.apache.lucene.document.Document doc) {
        return Optional.ofNullable(doc)
            .map(d -> ImmutablePodcastDTO.builder()
                .setEchoId(d.get(IndexField.ECHO_ID))
                .setTitle(d.get(IndexField.TITLE))
                .setLink(d.get(IndexField.LINK))
                .setPubDate(Optional.ofNullable(d.get(IndexField.PUB_DATE))
                    .map(DateMapper.INSTANCE::asLocalDateTime)
                    .orElse(null))
                .setDescription(Stream.of(d.get(IndexField.ITUNES_SUMMARY), d.get(IndexField.DESCRIPTION))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null))
                .setImage(d.get(IndexField.ITUNES_IMAGE))
                .create())
            .orElse(null);
    }

}
