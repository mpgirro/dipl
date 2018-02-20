package echo.core.mapper;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.exception.ConversionException;
import echo.core.model.domain.Podcast;
import echo.core.model.dto.PodcastDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Maximilian Irro
 */
@Mapper(uses={UrlMapper.class, DateMapper.class})
public interface PodcastMapper {

    PodcastMapper INSTANCE = Mappers.getMapper( PodcastMapper.class );

    @Mapping(target = "websiteData", ignore = true)
    PodcastDTO map(Podcast podcast);

    @Mapping(target = "episodes", ignore = true)
    @Mapping(target = "feeds", ignore = true)
    Podcast map(PodcastDTO podcastDto);

    default PodcastDTO map(org.apache.lucene.document.Document doc){

        if (doc == null) return null;

        final PodcastDTO dto = new PodcastDTO();

        if (doc.get("echo_id")      != null){ dto.setEchoId(doc.get("echo_id")); }
        if (doc.get("title")        != null){ dto.setTitle(doc.get("title")); }
        if (doc.get("link")         != null){ dto.setLink(doc.get("link")); }
        if (doc.get("description")  != null){ dto.setDescription(doc.get("description")); }
        if (doc.get("pub_date")     != null){ dto.setLastBuildDate(DateMapper.INSTANCE.asLocalDateTime(doc.get("pub_date"))); }
        if (doc.get("itunes_image") != null){ dto.setItunesImage(doc.get("itunes_image")); }

        return dto;
    }

    default PodcastDTO map(com.icosillion.podengine.models.Podcast podcast) throws ConversionException {

        if (podcast == null) return null;

        final PodcastDTO dto = new PodcastDTO();
        try {
            if (podcast.getTitle()         != null){ dto.setTitle(podcast.getTitle()); }
            if (podcast.getLink()          != null){ dto.setLink(podcast.getLink().toExternalForm()); }
            if (podcast.getDescription()   != null){ dto.setDescription(podcast.getDescription()); }
            if (podcast.getPubDate()       != null){ dto.setPubDate(LocalDateTime.ofInstant(podcast.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if (podcast.getLastBuildDate() != null){ dto.setLastBuildDate(LocalDateTime.ofInstant(podcast.getLastBuildDate().toInstant(), ZoneId.systemDefault())); }
            if (podcast.getLanguage()      != null){ dto.setLanguage(podcast.getLanguage()); }
            if (podcast.getGenerator()     != null){ dto.setGenerator(podcast.getGenerator()); }
            if (podcast.getCategories()    != null){ dto.setItunesCategories(new HashSet<>(Arrays.asList(podcast.getCategories()))); }
            if (podcast.getITunesInfo()    != null){ dto.setItunesImage(podcast.getITunesInfo().getImageString()); }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            throw new ConversionException("Exception during converting podengine.Podcast to PodcastDTO [reason: {}]", e);
        }

        return dto;
    }

}
