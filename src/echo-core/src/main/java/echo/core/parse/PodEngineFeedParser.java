package echo.core.parse;

import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Episode;
import com.icosillion.podengine.models.Podcast;
import echo.core.converter.DocumentConverter;
import echo.core.converter.PodEngineEpisodeConverter;
import echo.core.converter.PodEnginePodcastConverter;
import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;

/**
 * @author Maximilian Irro
 */
public class PodEngineFeedParser implements FeedParser {

    private final DocumentConverter podcastConverter;
    private final DocumentConverter episodeConverter;

    public PodEngineFeedParser(){
        this.podcastConverter = new PodEnginePodcastConverter();
        this.episodeConverter = new PodEngineEpisodeConverter();
    }

    @Override
    public PodcastDocument parseFeed(String xmlData) {

        Podcast podcast = null;
        try {
            podcast = new Podcast(xmlData);
        } catch (MalformedFeedException e) {
            e.printStackTrace();
        }

        return (PodcastDocument) podcastConverter.toEchoDocument(podcast);
    }

    @Override
    public EpisodeDocument parseEpisode(String xmlData) {
        throw new UnsupportedOperationException("PodEngineFeedParser.parseEpisode not yet implemented");
    }

    public EpisodeDocument[] extractEpisodes(String xmlData){
        try {
            final Podcast podcast = new Podcast(xmlData);
            return podcast.getEpisodes().stream()
                .map( e -> episodeConverter.toEchoDocument(e))
                .toArray(EpisodeDocument[]::new);
        } catch (MalformedFeedException e) {
            e.printStackTrace();
        }
        return new EpisodeDocument[0];
    }

}
