package echo.core.util;

import echo.core.model.dto.DTO;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.IndexResult;
import echo.core.model.dto.PodcastDTO;
import org.jsoup.*;

/**
 * @author Maximilian Irro
 */
public class DocumentFormatter {

    final StringBuilder builder = new StringBuilder();

    public static String cliFormat(DTO doc){
        final StringBuilder builder = new StringBuilder();
        if(doc instanceof PodcastDTO){
            final PodcastDTO podcast = (PodcastDTO) doc;
            appendString(builder, "[Podcast]");
            appendString(builder, podcast.getTitle());
            if(podcast.getPubDate() != null){
                builder.append(podcast.getPubDate());
            }
            appendString(builder, Jsoup.parse(podcast.getDescription()).text());
            appendString(builder, podcast.getLink());
        } else if( doc instanceof EpisodeDTO){
            final EpisodeDTO episode = (EpisodeDTO) doc;
            appendString(builder, "[Episode]");
            appendString(builder, episode.getTitle());
            if(episode.getPubDate() != null){
                appendString(builder, episode.getPubDate().toString());
            }
            if(episode.getItunesDuration() != null){
                appendString(builder, "Duration: "+episode.getItunesDuration().toString());
            }
            appendString(builder, Jsoup.parse(episode.getDescription()).text());
            appendString(builder, episode.getLink());
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

    private static void appendString(StringBuilder builder, String value) {
        builder.append(value + System.getProperty("line.separator"));
    }

}
