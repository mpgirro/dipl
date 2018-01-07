package echo.core.parse;

import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;

/**
 * @author Maximilian Irro
 */
public class SAXFeedParser implements FeedParser {

    /*
     * TODO here I plan to implement a custom feedparser for RSS/Atom feeds with the specific needs/extensions
     * (e.g. additional namespaces like fyyd: or psc:) for podcasts, using SAX feed parsing
     */

    @Override
    public PodcastDocument parseFeed(String xmlData) {
        throw new UnsupportedOperationException("SAXFeedParser.parseFeed not yet implemented");
    }

    @Override
    public EpisodeDocument parseEpisode(String xmlData) {
        throw new UnsupportedOperationException("SAXFeedParser.parseEpisode not yet implemented");
    }

}
