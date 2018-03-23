package echo.core.mapper;

import echo.core.domain.dto.immutable.ImmutableTestEpisode;
import echo.core.domain.dto.immutable.ModifiableTestEpisode;
import echo.core.domain.dto.immutable.TestEpisode;
import echo.core.domain.entity.Episode;
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
@Mapper(uses={TestPodcastMapper.class, TestChapterMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface TestEpisodeMapper {

    TestEpisodeMapper INSTANCE = Mappers.getMapper( TestEpisodeMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    ModifiableTestEpisode map(Episode episode);

    @Mapping(source = "podcastId", target = "podcast")
    Episode map(TestEpisode episode);

    ModifiableTestEpisode update(TestEpisode src, @MappingTarget ModifiableTestEpisode target);

    // TODO unused because we use PodcastMapper.class ?
    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

    default TestEpisode map(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final ImmutableTestEpisode.Builder builder = ImmutableTestEpisode.builder();

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

        return builder.create();
    }

}
