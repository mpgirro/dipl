package echo.core.util;

import echo.core.dto.document.Document;
import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;
import org.jsoup.*;

/**
 * @author Maximilian Irro
 */
public class DocumentFormatter {

    final StringBuilder builder = new StringBuilder();

    public String format(Document doc){
        if(doc instanceof PodcastDocument){
            final PodcastDocument pDoc = (PodcastDocument) doc;
            appendString("[Podcast]");
            appendString(pDoc.getTitle());
            appendString(pDoc.getLanguage());
            if(pDoc.getLastBuildDate() != null){
                appendString("Last built: " + pDoc.getLastBuildDate());
            }
            appendString(Jsoup.parse(pDoc.getDescription()).text());
            appendString(pDoc.getLink());
        } else if( doc instanceof EpisodeDocument){
            final EpisodeDocument eDoc = (EpisodeDocument) doc;
            appendString("[Episode]");
            appendString(eDoc.getTitle());
            if(eDoc.getPubDate() != null){
                appendString(eDoc.getPubDate().toString());
            }
            appendString(Jsoup.parse(eDoc.getDescription()).text());
            appendString(eDoc.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo Document type: "+doc.getClass());
        }
        return builder.toString();
    }

    private void appendString(String value) {
        builder.append(value + System.getProperty("line.separator"));
    }

}
