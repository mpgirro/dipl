package echo.core.parse.rss;

import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Podcast;
import echo.core.converter.DocumentConverter;
import echo.core.converter.PodEngineEpisodeConverter;
import echo.core.converter.PodEnginePodcastConverter;
import echo.core.dto.EpisodeDTO;
import echo.core.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;

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
    public PodcastDTO parseFeed(String xmlData) throws FeedParsingException {
        try {
            final Podcast podcast = new Podcast(xmlData);
            return (PodcastDTO) podcastConverter.toDTO(podcast);
        } catch (MalformedFeedException e) {
            throw new FeedParsingException("PodEngine could not parse the feed", e);
        }
    }

    @Override
    public EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("PodEngineFeedParser.parseEpisode not yet implemented");
    }

    public EpisodeDTO[] extractEpisodes(String xmlData) throws FeedParsingException {
        try {
            final Podcast podcast = new Podcast(xmlData);
            if(podcast.getEpisodes() != null){
                return podcast.getEpisodes().stream()
                    .map( e -> episodeConverter.toDTO(e))
                    .toArray(EpisodeDTO[]::new);
            }
        } catch (MalformedFeedException e) {
            throw new FeedParsingException("PodEngine could not parse the feed (trying to extract the episodes)", e);
        }
        return new EpisodeDTO[0];
    }

}
