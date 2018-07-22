package echo.microservice.parser.service;

import echo.core.async.catalog.ImmutableRegisterEpisodeIfNewJobCatalogJob;
import echo.core.async.catalog.ImmutableUpdatePodcastCatalogJob;
import echo.core.async.catalog.RegisterEpisodeIfNewJobCatalogJob;
import echo.core.async.catalog.UpdatePodcastCatalogJob;
import echo.core.async.parser.ParserJob;
import echo.core.benchmark.RoundTripTime;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.ModifiableEpisodeDTO;
import echo.core.domain.dto.ModifiablePodcastDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;
import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.PodcastMapper;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.RomeFeedParser;
import echo.microservice.parser.async.CatalogQueueSender;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
public class ParserService {

    private final Logger log = LoggerFactory.getLogger(ParserService.class);

    @Autowired
    private CatalogQueueSender catalogQueueSender;

    private final PodcastMapper podcastMapper = PodcastMapper.INSTANCE;
    private final EpisodeMapper episodeMapper = EpisodeMapper.INSTANCE;

    @Async
    public void parseFeed(String podcastExo, String feedUrl, String feedData, boolean isNewPodcast, RoundTripTime rtt) {
        try {
            final FeedParser parser = RomeFeedParser.of(feedData);
            final Optional<PodcastDTO> podcast = Optional.ofNullable(parser.getPodcast());
            if (podcast.isPresent()) {

                final ModifiablePodcastDTO p = podcastMapper.toModifiable(podcast.get());
                p.setExo(podcastExo);

                Optional.ofNullable(p.getTitle())
                    .ifPresent(t -> p.setTitle(t.trim()));
                Optional.ofNullable(p.getDescription())
                    .ifPresent(d -> p.setDescription(Jsoup.clean(d, Whitelist.basic())));

                if (isNewPodcast) {

                    if (!isNullOrEmpty(p.getLink())) {
                        // TODO tell crawler to download website content for podcast website
                    } else {
                        log.debug("No link set for podcast {} --> no website data will be added to the index", p.getExo());
                    }
                }

                final UpdatePodcastCatalogJob updatePodcastJob = ImmutableUpdatePodcastCatalogJob.of(p.toImmutable(), rtt.bumpRTTs());
                catalogQueueSender.produceMsg(updatePodcastJob);

                Optional
                    .ofNullable(parser.getEpisodes())
                    .ifPresent(es -> es.stream()
                        .map(episodeMapper::toModifiable)
                        .forEach(e -> {
                            Optional
                                .ofNullable(e.getTitle())
                                .ifPresent(t -> e.setTitle(t.trim()));
                            Optional
                                .ofNullable(e.getDescription())
                                .ifPresent(d -> e.setDescription(Jsoup.clean(d, Whitelist.basic())));
                            Optional
                                .ofNullable(e.getContentEncoded())
                                .ifPresent(c -> e.setContentEncoded(Jsoup.clean(c, Whitelist.basic())));

                            final RegisterEpisodeIfNewJobCatalogJob registerEpisodeJob = ImmutableRegisterEpisodeIfNewJobCatalogJob.of(podcastExo, e.toImmutable(), rtt.bumpRTTs());
                            catalogQueueSender.produceMsg(registerEpisodeJob);
                        }));
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

    @Async
    public void parseWebsite(String exo, String html) {

        final String readableText = Jsoup.parse(html).text();
        // TODO send to index to update doc
        throw new UnsupportedOperationException("ParsingService.parseWebsite");
    }

}
