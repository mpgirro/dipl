package echo.core.parse;

import echo.core.dto.document.EpisodeDTO;
import echo.core.dto.document.PodcastDTO;
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

}
