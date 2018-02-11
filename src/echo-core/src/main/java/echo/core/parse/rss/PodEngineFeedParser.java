package echo.core.parse.rss;

import com.icosillion.podengine.exceptions.MalformedFeedException;
import com.icosillion.podengine.models.Podcast;
import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.PodcastMapper;
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
            final PodcastDTO dto = PodcastMapper.INSTANCE.podenginePodcastToPodcastDto(podcast);
            dto.setLink(sanitizeUrl(dto.getLink()));
            return dto;
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
            if (podcast.getEpisodes() == null) {
                return new LinkedList<>();
            }
            final List<EpisodeDTO> episodes = EpisodeMapper.INSTANCE.podengineEpisodesToEpisodeDtos(podcast.getEpisodes());
            for (EpisodeDTO e : episodes) {
                e.setLink(sanitizeUrl(e.getLink()));
            }
            return episodes;
        } catch (MalformedFeedException | EchoException e) {
            throw new FeedParsingException("PodEngine could not parse the feed (trying to extract the episodes)", e);
        }
    }

}
