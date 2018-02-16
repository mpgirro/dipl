package echo.core.parse.rss;

import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public interface FeedParser {

    String NAMESPACE_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";
    String NAMESPACE_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";

    /*
     * TODO supertype for my planed custom SAX and DOM feed parsers
     */

    PodcastDTO parseFeed(String xmlData) throws FeedParsingException;

    EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException;

}
