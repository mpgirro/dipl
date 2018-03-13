package echo.microservice.parser.service;

import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.RomeFeedParser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
public class ParsingService {

    private final Logger log = LoggerFactory.getLogger(ParsingService.class);

    private final FeedParser feedParser = new RomeFeedParser();

    public void parseFeed(String podcastExo, String feedUrl, String feedData, Boolean isNewPodcast) {
        try {
            final Optional<PodcastDTO> podcast = Optional.ofNullable(feedParser.parseFeed(feedData));
            if (podcast.isPresent()) {

                final PodcastDTO p = podcast.get();
                p.setEchoId(podcastExo);

                if (isNullOrEmpty(p.getTitle())) {
                    p.setTitle(p.getTitle().trim());
                }

                if (isNullOrEmpty(p.getDescription())) {
                    final String d = Jsoup.clean(p.getDescription(), Whitelist.basic());
                    p.setDescription(d);
                }

                if (isNewPodcast) {
                    // TODO send podcast to index

                    if (isNullOrEmpty(p.getLink())) {
                        // TODO tell crawler to download website content for podcast website
                    }
                }

                // TODO tell catalog to update podcast metadata

                // TODO process episode data from feed
                throw new UnsupportedOperationException("ParsingService.parseFeed");
            } else {
                log.warn("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl);
            }

        } catch (FeedParsingException e) {
            log.error("FeedParsingException occured while processing feed: {}", feedUrl);

            // TODO : directoryStore ! FeedStatusUpdate(podcastId, feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

        } catch (StackOverflowError e) {
            log.error("StackOverflowError parsing: {}", feedUrl);
        }
    }

    public void parseWebsite(String exo, String html) {
        final String readableText = Jsoup.parse(html).text();
        // TODO send to index to update doc
        throw new UnsupportedOperationException("ParsingService.parseWebsite");
    }

    private void processEpisodes(String podcastExo, String feedUrl, String feedData) throws FeedParsingException {
        final EpisodeDTO[] episodes = feedParser.extractEpisodes(feedData);
        for (EpisodeDTO e : episodes) {
            if (isNullOrEmpty(e.getTitle())) {
                e.setTitle(e.getTitle().trim());
            }

            if (isNullOrEmpty(e.getDescription())) {
                e.setDescription(Jsoup.clean(e.getDescription(), Whitelist.basic()));
            }

            if (isNullOrEmpty(e.getContentEncoded())) {
                e.setContentEncoded(Jsoup.clean(e.getContentEncoded(), Whitelist.basic()));
            }

            // TODO tell catalog to register episode if unknown
            throw new UnsupportedOperationException("ParsingService.processEpisodes");
        }
    }

}
