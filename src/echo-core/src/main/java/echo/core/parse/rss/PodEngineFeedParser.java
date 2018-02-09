package echo.core.parse.rss;

import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Podcast;
import echo.core.converter.mapper.EpisodeMapper;
import echo.core.converter.mapper.PodcastMapper;
import echo.core.exception.EchoException;
import echo.core.exception.FeedParsingException;
import echo.core.model.dto.EpisodeDTO;
import echo.core.model.dto.PodcastDTO;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Irro
 */
public class PodEngineFeedParser implements FeedParser {

    @Override
    public PodcastDTO parseFeed(String xmlData) throws FeedParsingException {
        try {
            final Podcast podcast = new Podcast(xmlData);
            return PodcastMapper.INSTANCE.podenginePodcastToPodcastDto(podcast);
        } catch (MalformedFeedException | EchoException e) {
            throw new FeedParsingException("PodEngine could not parse the feed", e);
        }
    }

    @Override
    public EpisodeDTO parseEpisode(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("PodEngineFeedParser.parseEpisode not yet implemented");
    }

    public List<EpisodeDTO> extractEpisodes(String xmlData) throws FeedParsingException {
        try {
            final Podcast podcast = new Podcast(xmlData);
            if(podcast.getEpisodes() != null){
                return EpisodeMapper.INSTANCE.podengineEpisodesToEpisodeDtos(podcast.getEpisodes());
            }
        } catch (MalformedFeedException | EchoException e) {
            throw new FeedParsingException("PodEngine could not parse the feed (trying to extract the episodes)", e);
        }
        return new LinkedList<>();
    }

}
