package echo.core.parse;

import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;

/**
 * @author Maximilian Irro
 */
public interface FeedParser {

    /*
     * TODO supertype for my planed custom SAX and DOM feed parsers
     */

    PodcastDocument parseFeed(String xmlData);

    EpisodeDocument parseEpisode(String xmlData);

}
