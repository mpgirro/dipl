package echo.core.mapper;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.exception.ConversionException;
import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import echo.core.domain.dto.EpisodeDTO;
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

        if (doc.get("echo_id")         != null) { dto.setEchoId(doc.get("echo_id")); }
        if (doc.get("title")           != null) { dto.setTitle(doc.get("title")); }
        if (doc.get("link")            != null) { dto.setLink(doc.get("link")); }
        if (doc.get("pub_date")        != null) { dto.setPubDate(DateMapper.INSTANCE.asLocalDateTime(doc.get("pub_date"))); }
        if (doc.get("description")     != null) { dto.setDescription(doc.get("description")); }
        if (doc.get("itunes_image")    != null) { dto.setItunesImage(doc.get("itunes_image")); }
        if (doc.get("itunes_duration") != null) { dto.setItunesDuration(doc.get("itunes_duration")); }

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
