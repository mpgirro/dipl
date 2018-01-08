package echo.core.parse;

import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public interface FeedParser {

    /*
     * TODO supertype for my planed custom SAX and DOM feed parsers
     */

    PodcastDocument parseFeed(String xmlData) throws FeedParsingException;

    EpisodeDocument parseEpisode(String xmlData) throws FeedParsingException;

}
