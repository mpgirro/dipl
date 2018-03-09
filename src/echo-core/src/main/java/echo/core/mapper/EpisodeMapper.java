package echo.core.mapper;

import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.index.IndexField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, ChapterMapper.class, DateMapper.class})
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    EpisodeDTO map(Episode episode);

    @Mapping(source = "podcastId", target = "podcast")
    Episode map(EpisodeDTO episodeDto);

    // TODO unused because we use PodcastMapper.class ?
    default Podcast podcastFromId(Long id) {

        if (id == null) return null;

        final Podcast podcast = new Podcast();
        podcast.setId(id);

        return podcast;
    }

    default EpisodeDTO map(org.apache.lucene.document.Document doc){

        if (doc == null) return null;

        final EpisodeDTO dto = new EpisodeDTO();

        if (doc.get(IndexField.ECHO_ID)         != null) { dto.setEchoId(doc.get(IndexField.ECHO_ID)); }
        if (doc.get(IndexField.TITLE)           != null) { dto.setTitle(doc.get(IndexField.TITLE)); }
        if (doc.get(IndexField.LINK)            != null) { dto.setLink(doc.get(IndexField.LINK)); }
        if (doc.get(IndexField.PUB_DATE)        != null) { dto.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get(IndexField.PUB_DATE))); }
        if (doc.get(IndexField.PODCAST_TITLE)   != null) { dto.setPodcastTitle(doc.get(IndexField.PODCAST_TITLE)); }
        if (doc.get(IndexField.ITUNES_SUMMARY) != null) {
            dto.setDescription(doc.get(IndexField.ITUNES_SUMMARY));
        } else if (doc.get(IndexField.DESCRIPTION) != null) {
            dto.setDescription(doc.get(IndexField.DESCRIPTION));
        }
        if (doc.get(IndexField.ITUNES_IMAGE)    != null) { dto.setImage(doc.get(IndexField.ITUNES_IMAGE)); }
        if (doc.get(IndexField.ITUNES_DURATION) != null) { dto.setItunesDuration(doc.get(IndexField.ITUNES_DURATION)); }

        return dto;
    }

}
