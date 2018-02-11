package echo.core.parse.rss;

import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public interface FeedParser {

    /*
     * TODO supertype for my planed custom SAX and DOM feed parsers
     */

    PodcastDTO parseFeed(String xmlData) throws FeedParsingException;

    EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException;

    default String sanitizeUrl(String url) {
        if(url == null){
            return null;
        }

        return url
            .replace("\n", "")
            .replace("\t", "")
            .replace("\r", "");
    }

}
