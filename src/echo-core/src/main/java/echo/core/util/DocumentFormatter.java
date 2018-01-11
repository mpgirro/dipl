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

    public static String cliFormat(Document doc){
        final StringBuilder builder = new StringBuilder();
        if(doc instanceof PodcastDocument){
            final PodcastDocument pDoc = (PodcastDocument) doc;
            appendString(builder, "[Podcast]");
            appendString(builder, pDoc.getTitle());
            appendString(builder, pDoc.getLanguage());
            if(pDoc.getLastBuildDate() != null){
                appendString(builder, "Last built: " + pDoc.getLastBuildDate());
            }
            appendString(builder, Jsoup.parse(pDoc.getDescription()).text());
            appendString(builder, pDoc.getLink());
        } else if( doc instanceof EpisodeDocument){
            final EpisodeDocument eDoc = (EpisodeDocument) doc;
            appendString(builder, "[Episode]");
            appendString(builder, eDoc.getTitle());
            if(eDoc.getPubDate() != null){
                appendString(builder, eDoc.getPubDate().toString());
            }
            appendString(builder, Jsoup.parse(eDoc.getDescription()).text());
            appendString(builder, eDoc.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo Document type: "+doc.getClass());
        }
        return builder.toString();
    }

    public static Document stripHTML(Document doc){
        if(doc instanceof PodcastDocument){
            final PodcastDocument pDoc = (PodcastDocument) doc;
            pDoc.setDescription(Jsoup.parse(pDoc.getDescription()).text());
            return pDoc;
        } else if( doc instanceof EpisodeDocument){
            final EpisodeDocument eDoc = (EpisodeDocument) doc;
            eDoc.setDescription(Jsoup.parse(eDoc.getDescription()).text());
            return eDoc;
        } else {
            throw new RuntimeException("Forgot to support new Echo Document type: "+doc.getClass());
        }
    }

    private static void appendString(StringBuilder builder, String value) {
        builder.append(value + System.getProperty("line.separator"));
    }

}
