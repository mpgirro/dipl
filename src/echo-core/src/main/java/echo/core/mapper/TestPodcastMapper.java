package echo.core.mapper;

import echo.core.domain.dto.immutable.ImmutableTestPodcast;
import echo.core.domain.dto.immutable.ModifiableTestPodcast;
import echo.core.domain.dto.immutable.TestPodcast;
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
public interface TestPodcastMapper {

    TestPodcastMapper INSTANCE = Mappers.getMapper( TestPodcastMapper.class );

    ModifiableTestPodcast map(Podcast podcast);

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast map(TestPodcast dto);

    ModifiableTestPodcast update(TestPodcast src, @MappingTarget ModifiableTestPodcast target);

    default TestPodcast map(org.apache.lucene.document.Document doc) {

        if (doc == null) return null;

        final ImmutableTestPodcast.Builder builder = ImmutableTestPodcast.builder();

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
