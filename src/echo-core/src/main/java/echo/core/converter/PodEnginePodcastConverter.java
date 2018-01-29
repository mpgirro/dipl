package echo.core.converter;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Podcast;
import echo.core.model.dto.PodcastDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * @author Maximilian Irro
 */
public class PodEnginePodcastConverter extends DocumentConverter<PodcastDTO,Podcast> {

    private static final Logger log = LoggerFactory.getLogger(PodEnginePodcastConverter.class);

    @Override
    public PodcastDTO toDTO(Podcast podcast) {

        final PodcastDTO doc = new PodcastDTO();
        try {
            if(podcast.getTitle()         != null){ doc.setTitle(podcast.getTitle()); }
            if(podcast.getLink()          != null){ doc.setLink(podcast.getLink().toExternalForm()); }
            if(podcast.getDescription()   != null){ doc.setDescription(podcast.getDescription()); }
            if(podcast.getPubDate()       != null){ doc.setPubDate(LocalDateTime.ofInstant(podcast.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if(podcast.getLastBuildDate() != null){ doc.setLastBuildDate(LocalDateTime.ofInstant(podcast.getLastBuildDate().toInstant(), ZoneId.systemDefault())); }
            if(podcast.getLanguage()      != null){ doc.setLanguage(podcast.getLanguage()); }
            if(podcast.getGenerator()     != null){ doc.setGenerator(podcast.getGenerator()); }
            if(podcast.getCategories()    != null){ doc.setItunesCategory(String.join(" & ", Arrays.asList(podcast.getCategories()))); }
            if(podcast.getITunesInfo()    != null){ doc.setItunesImage(podcast.getITunesInfo().getImageString()); }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            log.error("Exception during converting PodEnginePodcast to PodcastDTO; reason: {}", e.getMessage());
            //e.printStackTrace();
        }

        return doc;
    }

    @Override
    public Podcast toIndex(PodcastDTO doc) {
        throw new UnsupportedOperationException("PodEnginePodcastConverter.toEntity is not available, due to PodEngine library restrictions");
    }

}
