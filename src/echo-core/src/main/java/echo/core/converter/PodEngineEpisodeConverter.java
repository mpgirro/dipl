package echo.core.converter;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Episode;
import echo.core.dto.document.EpisodeDocument;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public class PodEngineEpisodeConverter extends DocumentConverter<EpisodeDocument,Episode> {

    @Override
    public EpisodeDocument toEchoDocument(Episode episode) {

        final EpisodeDocument doc = new EpisodeDocument();
        try {
            doc.setTitle(episode.getTitle());
            if(episode.getLink() != null){
                doc.setLink(episode.getLink().toExternalForm());
            }
            if(episode.getPubDate() != null){
                doc.setPubDate(LocalDateTime.ofInstant(episode.getPubDate().toInstant(), ZoneId.systemDefault()));
            }
            doc.setGuid(episode.getGUID());
            doc.setDescription(episode.getDescription());
        } catch (MalformedFeedException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (DateFormatException e) {
            e.printStackTrace();
        }

        return doc;
    }

    @Override
    public Episode toEntityDocument(EpisodeDocument doc) {
        throw new UnsupportedOperationException("PodEngineEpisodeConverter.toEntityDocument is not available, due to PodEngine library restrictions");
    }

}
