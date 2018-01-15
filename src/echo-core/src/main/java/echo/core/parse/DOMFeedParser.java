package echo.core.parse;

import echo.core.dto.document.EpisodeDTO;
import echo.core.dto.document.PodcastDTO;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public class DOMFeedParser implements FeedParser {

    /*
     * TODO here I plan to implement a custom feedparser for RSS/Atom feeds with the specific needs/extensions
     * (e.g. additional namespaces like fyyd: or psc:) for podcasts, using DOM feed parsing
     */

    @Override
    public PodcastDTO parseFeed(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("DOMFeedParser.parseFeed not yet implemented");
    }

    @Override
    public EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("DOMFeedParser.parseEpisode not yet implemented");
    }

}
