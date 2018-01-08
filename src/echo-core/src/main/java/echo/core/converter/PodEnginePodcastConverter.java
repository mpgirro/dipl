package echo.core.converter;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Podcast;
import echo.core.dto.document.PodcastDocument;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Maximilian Irro
 */
public class PodEnginePodcastConverter extends DocumentConverter<PodcastDocument,Podcast> {

    @Override
    public PodcastDocument toEchoDocument(Podcast podcast) {

        final PodcastDocument doc = new PodcastDocument();
        try {
            doc.setTitle(podcast.getTitle());
            doc.setLink(podcast.getLink().toExternalForm());
            doc.setDescription(podcast.getDescription());
            if(podcast.getLastBuildDate() != null){
                doc.setLastBuildDate(LocalDateTime.ofInstant(podcast.getLastBuildDate().toInstant(), ZoneId.systemDefault()));
            }
            doc.setLanguage(podcast.getLanguage());
            doc.setGenerator(podcast.getGenerator());
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
    public Podcast toEntityDocument(PodcastDocument doc) {
        throw new UnsupportedOperationException("PodEnginePodcastConverter.toEntityDocument is not available, due to PodEngine library restrictions");
    }

}
