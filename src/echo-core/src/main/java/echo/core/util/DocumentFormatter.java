package echo.core.util;

import echo.core.dto.DTO;
import echo.core.dto.EpisodeDTO;
import echo.core.dto.IndexResult;
import echo.core.dto.PodcastDTO;
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

    public static String cliFormat(IndexResult result){
        final StringBuilder builder = new StringBuilder();
        if(result.getDocType().equals("podcast")){
            appendString(builder, "[Podcast]");
            appendString(builder, result.getTitle());
            if(result.getPubDate() != null){
                builder.append(result.getPubDate());
            }
            appendString(builder, Jsoup.parse(result.getDescription()).text());
            appendString(builder, result.getLink());
        } else if(result.getDocType().equals("episode")){
            appendString(builder, "[Episode]");
            appendString(builder, result.getTitle());
            if(result.getPubDate() != null){
                appendString(builder, result.getPubDate().toString());
            }
            /* TODO
            if(result.getItunesDuration() != null){
                appendString(builder, "Duration: "+result.getItunesDuration().toString());
            }
            */
            appendString(builder, Jsoup.parse(result.getDescription()).text());
            appendString(builder, result.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+result.getClass());
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
