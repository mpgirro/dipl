package echo.core.util;

import echo.core.model.dto.DTO;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.IndexDocDTO;
import echo.core.model.dto.PodcastDTO;
import org.jsoup.*;

/**
 * @author Maximilian Irro
 */
public class DocumentFormatter {

    final StringBuilder builder = new StringBuilder();

    public static String cliFormat(DTO dto){
        final StringBuilder builder = new StringBuilder();
        if(dto instanceof PodcastDTO){
            final PodcastDTO podcast = (PodcastDTO) dto;
            appendString(builder, "[Podcast]");
            appendString(builder, podcast.getTitle());
            if(podcast.getPubDate() != null){
                builder.append(podcast.getPubDate());
            }
            appendString(builder, Jsoup.parse(podcast.getDescription()).text());
            appendString(builder, podcast.getLink());
        } else if(dto instanceof EpisodeDTO){
            final EpisodeDTO episode = (EpisodeDTO) dto;
            appendString(builder, "[Episode]");
            appendString(builder, episode.getTitle());
            if(episode.getPubDate() != null){
                appendString(builder, episode.getPubDate().toString());
            }
            if(episode.getItunesDuration() != null){
                appendString(builder, "Duration: "+ episode.getItunesDuration());
            }
            appendString(builder, Jsoup.parse(episode.getDescription()).text());
            appendString(builder, episode.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+dto.getClass());
        }
        return builder.toString();
    }

    public static String cliFormat(IndexDocDTO doc){
        final StringBuilder builder = new StringBuilder();
        if(doc.getDocType().equals("podcast")){
            appendString(builder, "[Podcast]");
            appendString(builder, doc.getTitle());
            if(doc.getPubDate() != null){
                builder.append(doc.getPubDate());
            }
            appendString(builder, Jsoup.parse(doc.getDescription()).text());
            appendString(builder, doc.getLink());
        } else if(doc.getDocType().equals("episode")){
            appendString(builder, "[Episode]");
            appendString(builder, doc.getTitle());
            if(doc.getPubDate() != null){
                appendString(builder, doc.getPubDate().toString());
            }
            /* TODO
            if(result.getItunesDuration() != null){
                appendString(builder, "Duration: "+result.getItunesDuration().toString());
            }
            */
            appendString(builder, Jsoup.parse(doc.getDescription()).text());
            appendString(builder, doc.getLink());
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+doc.getClass());
        }
        return builder.toString();
    }

    /*
    public static DTO stripHTML(DTO doc){
        if(doc instanceof PodcastDTO){
            final PodcastDTO podcast = (PodcastDTO) doc;
            podcast.setDescription(Jsoup.parse(podcast.getDescription()).text());
            return podcast;
        } else if( doc instanceof EpisodeDTO){
            final EpisodeDTO episode = (EpisodeDTO) doc;
            episode.setDescription(Jsoup.parse(episode.getDescription()).text());
            return episode;
        } else {
            throw new RuntimeException("Forgot to support new Echo DTO type: "+doc.getClass());
        }
    }
    */

    private static void appendString(StringBuilder builder, String value) {
        builder.append(value + System.getProperty("line.separator"));
    }

}
