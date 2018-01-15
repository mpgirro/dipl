package echo.core.util;

import echo.core.dto.document.DTO;
import echo.core.dto.document.EpisodeDTO;
import echo.core.dto.document.PodcastDTO;
import org.jsoup.*;

/**
 * @author Maximilian Irro
 */
public class DocumentFormatter {

    final StringBuilder builder = new StringBuilder();

    public static String cliFormat(DTO doc){
        final StringBuilder builder = new StringBuilder();
        if(doc instanceof PodcastDTO){
            final PodcastDTO pDoc = (PodcastDTO) doc;
            appendString(builder, "[Podcast]");
            appendString(builder, pDoc.getTitle());
            if(pDoc.getPubDate() != null){
                builder.append(pDoc.getPubDate());
            }
            appendString(builder, Jsoup.parse(pDoc.getDescription()).text());
            appendString(builder, pDoc.getLink());
        } else if( doc instanceof EpisodeDTO){
            final EpisodeDTO eDoc = (EpisodeDTO) doc;
            appendString(builder, "[Episode]");
            appendString(builder, eDoc.getTitle());
            if(eDoc.getPubDate() != null){
                appendString(builder, eDoc.getPubDate().toString());
            }
            if(eDoc.getItunesDuration() != null){
                appendString(builder, "Duration: "+eDoc.getItunesDuration().toString());
            }
            appendString(builder, Jsoup.parse(eDoc.getDescription()).text());
            appendString(builder, eDoc.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+doc.getClass());
        }
        return builder.toString();
    }

    public static DTO stripHTML(DTO doc){
        if(doc instanceof PodcastDTO){
            final PodcastDTO pDoc = (PodcastDTO) doc;
            pDoc.setDescription(Jsoup.parse(pDoc.getDescription()).text());
            return pDoc;
        } else if( doc instanceof EpisodeDTO){
            final EpisodeDTO eDoc = (EpisodeDTO) doc;
            eDoc.setDescription(Jsoup.parse(eDoc.getDescription()).text());
            return eDoc;
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+doc.getClass());
        }
    }

    private static void appendString(StringBuilder builder, String value) {
        builder.append(value + System.getProperty("line.separator"));
    }

}
