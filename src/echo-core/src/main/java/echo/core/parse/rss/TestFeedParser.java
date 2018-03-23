package echo.core.parse.rss;

import echo.core.domain.dto.immutable.TestEpisode;
import echo.core.domain.dto.immutable.TestPodcast;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public interface TestFeedParser {

    String NAMESPACE_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd";
    String NAMESPACE_CONTENT = "http://purl.org/rss/1.0/modules/content/";
    String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
    String NAMESPACE_PSC = "http://podlove.org/simple-chapters";

    /*
     * TODO supertype for my planed custom SAX and DOM feed parsers
     */

    TestPodcast parseFeed(String xmlData) throws FeedParsingException;

    TestEpisode[] extractEpisodes(String xmlData) throws FeedParsingException;

}
