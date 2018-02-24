package echo.core.mapper;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.exception.ConversionException;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.index.IndexField;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={PodcastMapper.class, ChapterMapper.class, DateMapper.class})
public interface EpisodeMapper {

    EpisodeMapper INSTANCE = Mappers.getMapper( EpisodeMapper.class );

    @Mapping(source = "podcast.id", target = "podcastId")
    @Mapping(source = "podcast.echoId", target = "podcastEchoId")
    @Mapping(source = "podcast.title", target = "podcastTitle")
    @Mapping(target = "websiteData", ignore = true)
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
        if (doc.get(IndexField.ITUNES_IMAGE)    != null) { dto.setItunesImage(doc.get(IndexField.ITUNES_IMAGE)); }
        if (doc.get(IndexField.ITUNES_DURATION) != null) { dto.setItunesDuration(doc.get(IndexField.ITUNES_DURATION)); }

        return dto;
    }

    default EpisodeDTO map(com.icosillion.podengine.models.Episode episode) throws ConversionException {

        if (episode == null) return null;

        final EpisodeDTO dto = new EpisodeDTO();
        try {
            if (episode.getTitle()       != null) { dto.setTitle(episode.getTitle()); }
            if (episode.getLink()        != null) { dto.setLink(episode.getLink().toExternalForm()); }
            if (episode.getPubDate()     != null) { dto.setPubDate(LocalDateTime.ofInstant(episode.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if (episode.getGUID()        != null) { dto.setGuid(episode.getGUID()); }
            if (episode.getDescription() != null) { dto.setDescription(episode.getDescription()); }
            if (episode.getITunesInfo()  != null) {
                if (episode.getITunesInfo().getImageString() != null) { dto.setItunesImage(episode.getITunesInfo().getImageString()); }
                if (episode.getITunesInfo().getDuration()    != null) { dto.setItunesDuration(episode.getITunesInfo().getDuration()); }
            }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            throw new ConversionException("Exception during converting podengine.Episode to EpisodeDTO [reason: {}]", e);
        }

        return dto;
    }

    List<EpisodeDTO> map(List<com.icosillion.podengine.models.Episode> episodes) throws ConversionException;

}
