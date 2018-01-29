package echo.core.parse.rss;

import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;

/**
 * @author Maximilian Irro
 */
public class SAXFeedParser implements FeedParser {

    /*
     * TODO here I plan to implement a custom feedparser for RSS/Atom feeds with the specific needs/extensions
     * (e.g. additional namespaces like fyyd: or psc:) for podcasts, using SAX feed parsing
     */

    @Override
    public PodcastDTO parseFeed(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("SAXFeedParser.parseFeed not yet implemented");
    }

    @Override
    public EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("SAXFeedParser.parseEpisode not yet implemented");
    }

}
