package echo.microservice.parser.service;

import echo.core.async.job.EpisodeRegisterJob;
import echo.core.async.job.ParserJob;
import echo.core.async.job.UpdatePodcastCatalogJob;
import echo.core.domain.dto.*;
import echo.core.exception.FeedParsingException;
import echo.core.mapper.EpisodeMapper;
import echo.core.mapper.IndexMapper;
import echo.core.mapper.PodcastMapper;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.RomeFeedParser;
import echo.microservice.parser.async.CatalogQueueSender;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
public class ParserService {

    private final Logger log = LoggerFactory.getLogger(ParserService.class);

    @Autowired
    private CatalogQueueSender catalogQueueSender;

    private final String CATALOG_URL = "http://localhost:3031"; // TODO
    private final String INDEX_URL = "http://localhost:3032"; // TODO

    private final FeedParser feedParser = new RomeFeedParser();

    private final IndexMapper indexMapper = IndexMapper.INSTANCE;
    private final PodcastMapper podcastMapper = PodcastMapper.INSTANCE;
    private final EpisodeMapper episodeMapper = EpisodeMapper.INSTANCE;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void parseFeed(ParserJob parserJob, Boolean isNewPodcast) {
        final String podcastExo = parserJob.getExo();
        final String feedUrl = parserJob.getUrl();
        final String feedData = parserJob.getData();
        try {
            final Optional<PodcastDTO> podcast = Optional.ofNullable(feedParser.parseFeed(feedData));
            if (podcast.isPresent()) {

                final ModifiablePodcastDTO p = podcastMapper.toModifiable(podcast.get());
                p.setEchoId(podcastExo);

                Optional.ofNullable(p.getTitle())
                    .ifPresent(t -> p.setTitle(t.trim()));
                Optional.ofNullable(p.getDescription())
                    .ifPresent(d -> p.setDescription(Jsoup.clean(d, Whitelist.basic())));

                if (isNewPodcast) {

                    if (!isNullOrEmpty(p.getLink())) {
                        // TODO tell crawler to download website content for podcast website
                    } else {
                        log.debug("No link set for podcast {} --> no website data will be added to the index", p.getEchoId());
                    }
                }

                // TODO
                //catalogQueueSender.produceMsg("<Update-Podcast : " + p.getEchoId() + ">");
                final UpdatePodcastCatalogJob catalogJo = new UpdatePodcastCatalogJob(p.toImmutable());
                catalogQueueSender.produceMsg(catalogJo);

                /*
                // TODO replace by async job?
                // tell catalog to update podcast metadata
                final String catalogUpdateUrl = CATALOG_URL+"/catalog/podcast";
                log.debug("Sending podcast for update to catalog with request : {}", catalogUpdateUrl);
                restTemplate.put(catalogUpdateUrl, p.toImmutable());
                */

                processEpisodes(podcastExo, feedData);
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
    public void parseWebsite(ParserJob job) {
        final String exo = job.getExo();
        final String html = job.getData();

        final String readableText = Jsoup.parse(html).text();
        // TODO send to index to update doc
        throw new UnsupportedOperationException("ParsingService.parseWebsite");
    }

    private void processEpisodes(String podcastExo, String feedData) throws FeedParsingException {
        final List<ModifiableEpisodeDTO> episodes = feedParser.extractEpisodes(feedData).stream()
            .map(episodeMapper::toModifiable)
            .collect(Collectors.toList());
        for (ModifiableEpisodeDTO e : episodes) {

            Optional.ofNullable(e.getTitle()).ifPresent(t -> e.setTitle(t.trim()));
            Optional.ofNullable(e.getDescription()).ifPresent(d -> e.setDescription(Jsoup.clean(d, Whitelist.basic())));
            Optional.ofNullable(e.getContentEncoded()).ifPresent(c -> e.setContentEncoded(Jsoup.clean(c, Whitelist.basic())));

            // TODO
            //catalogQueueSender.produceMsg("<Register-Episode-If-Unknown : " + e.getTitle() + ">");
            final EpisodeRegisterJob job = new EpisodeRegisterJob(podcastExo, e.toImmutable());
            catalogQueueSender.produceMsg(job);

            /*
            // TODO replace by async job?
            // tell catalog to register episode if unknown
            final String catalogRegistrationUrl = CATALOG_URL+"/catalog/episode/register";
            log.debug("Sending episode for registration to catalog with request : {}", catalogRegistrationUrl);
            final EpisodeRegisterJob job = new EpisodeRegisterJob();
            job.setPodcastExo(podcastExo);
            job.setEpisode(e.toImmutable());
            final HttpEntity<EpisodeRegisterJob> request = new HttpEntity<>(job);
            restTemplate.postForEntity(catalogRegistrationUrl, request, EpisodeRegisterJob.class);
            */
        }
    }

}
