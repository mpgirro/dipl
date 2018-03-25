package echo.core.mapper;

import echo.core.domain.dto.ImmutableEpisodeDTO;
import echo.core.domain.dto.ModifiableEpisodeDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.entity.Episode;
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
@Mapper(uses={PodcastMapper.class, ChapterMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    ModifiableEpisodeDTO toModifiable(Episode episode);

    default ImmutableEpisodeDTO toImmutable(Episode episode) {
        return Optional.ofNullable(episode)
            .map(e -> toModifiable(e).toImmutable())
            .orElse(null);
    }

    default ImmutableEpisodeDTO toImmutable(EpisodeDTO episode) {

        if (episode == null) return null;

        if (episode instanceof  ImmutableEpisodeDTO) {
            return (ImmutableEpisodeDTO) episode;
        }
        return ((ModifiableEpisodeDTO) episode).toImmutable();
    }

    @Mapping(source = "podcastId", target = "podcast")
    Episode toEntity(EpisodeDTO episode);

    ModifiableEpisodeDTO update(EpisodeDTO src, @MappingTarget ModifiableEpisodeDTO target);

    // TODO
    default ModifiableEpisodeDTO update(EpisodeDTO src, @MappingTarget EpisodeDTO target) {

        if (target == null) return null;

        ModifiableEpisodeDTO modTarget;
        if (target instanceof  ModifiableEpisodeDTO) {
            modTarget = (ModifiableEpisodeDTO) target;
        } else {
            modTarget = new ModifiableEpisodeDTO().from(target);
        }
        return update(src, modTarget);
    }

    default ImmutableEpisodeDTO updateImmutable(EpisodeDTO src, @MappingTarget EpisodeDTO target) {

        if (target == null) return null;

        ModifiableEpisodeDTO modTarget;
        if (target instanceof  ModifiableEpisodeDTO) {
            modTarget = (ModifiableEpisodeDTO) target;
        } else {
            modTarget = new ModifiableEpisodeDTO().from(target);
        }
        return update(src, modTarget).toImmutable();
    }

    // TODO unused because we use PodcastMapper.class ?
    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

    default ImmutableEpisodeDTO toImmutable(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final ImmutableEpisodeDTO.Builder builder = ImmutableEpisodeDTO.builder();

        Optional.ofNullable(doc.get(IndexField.ECHO_ID))
            .ifPresent(builder::setEchoId);
        Optional.ofNullable(doc.get(IndexField.TITLE))
            .ifPresent(builder::setTitle);
        Optional.ofNullable(doc.get(IndexField.LINK))
            .ifPresent(builder::setLink);
        Optional.ofNullable(doc.get(IndexField.PUB_DATE))
            .map(DateMapper.INSTANCE::asLocalDateTime)
            .ifPresent(builder::setPubDate);
        Optional.ofNullable(doc.get(IndexField.PODCAST_TITLE))
            .ifPresent(builder::setPodcastTitle);
        Optional.ofNullable(Stream.of(doc.get(IndexField.ITUNES_SUMMARY), doc.get(IndexField.DESCRIPTION))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null))
            .ifPresent(builder::setDescription);
        Optional.ofNullable(doc.get(IndexField.ITUNES_IMAGE))
            .ifPresent(builder::setImage);
        Optional.ofNullable(doc.get(IndexField.ITUNES_DURATION))
            .ifPresent(builder::setItunesDuration);

        /*
        if (doc.get(IndexField.ECHO_ID)         != null) { builder.setEchoId(doc.get(IndexField.ECHO_ID)); }
        if (doc.get(IndexField.TITLE)           != null) { builder.setTitle(doc.get(IndexField.TITLE)); }
        if (doc.get(IndexField.LINK)            != null) { builder.setLink(doc.get(IndexField.LINK)); }
        if (doc.get(IndexField.PUB_DATE)        != null) { builder.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get(IndexField.PUB_DATE))); }
        if (doc.get(IndexField.PODCAST_TITLE)   != null) { builder.setPodcastTitle(doc.get(IndexField.PODCAST_TITLE)); }
        if (doc.get(IndexField.ITUNES_SUMMARY)  != null) {
            builder.setDescription(doc.get(IndexField.ITUNES_SUMMARY));
        } else if (doc.get(IndexField.DESCRIPTION) != null) {
            builder.setDescription(doc.get(IndexField.DESCRIPTION));
        }
        if (doc.get(IndexField.ITUNES_IMAGE)    != null) { builder.setImage(doc.get(IndexField.ITUNES_IMAGE)); }
        if (doc.get(IndexField.ITUNES_DURATION) != null) { builder.setItunesDuration(doc.get(IndexField.ITUNES_DURATION)); }
        */

        return builder.create();
    }

}
