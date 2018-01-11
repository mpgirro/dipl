package echo.core.converter;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Episode;
import echo.core.dto.document.EpisodeDocument;
import echo.core.exception.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public class PodEngineEpisodeConverter extends DocumentConverter<EpisodeDocument,Episode> {

    private static final Logger log = LoggerFactory.getLogger(PodEngineEpisodeConverter.class);

    @Override
    public EpisodeDocument toEchoDocument(Episode episode) {

        final EpisodeDocument doc = new EpisodeDocument();
        try {
            if(episode.getTitle()       != null){ doc.setTitle(episode.getTitle()); }
            if(episode.getLink()        != null){ doc.setLink(episode.getLink().toExternalForm()); }
            if(episode.getPubDate()     != null){ doc.setPubDate(LocalDateTime.ofInstant(episode.getPubDate().toInstant(), ZoneId.systemDefault())); }
            if(episode.getGUID()        != null){ doc.setGuid(episode.getGUID()); }
            if(episode.getDescription() != null){ doc.setDescription(episode.getDescription()); }
            if(episode.getITunesInfo()  != null){
                if(episode.getITunesInfo().getImageString() != null){ doc.setItunesImage(episode.getITunesInfo().getImageString()); }
                if(episode.getITunesInfo().getDuration()    != null){ doc.setItunesDuration(episode.getITunesInfo().getDuration()); }
            }
        } catch (MalformedFeedException | MalformedURLException | DateFormatException e) {
            log.error("Exception during converting PodEngineEpisode to EpisodeDocument; reason: {}", e.getMessage());
            //e.printStackTrace();
        }

        return doc;
    }

    @Override
    public Episode toEntityDocument(EpisodeDocument doc) {
        throw new UnsupportedOperationException("PodEngineEpisodeConverter.toEntityDocument is not available, due to PodEngine library restrictions");
    }

}
