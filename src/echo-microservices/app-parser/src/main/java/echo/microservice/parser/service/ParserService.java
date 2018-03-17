package echo.microservice.parser.service;

import echo.core.async.job.EpisodeRegisterJob;
import echo.core.async.job.ParserJob;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;
import echo.core.mapper.IndexMapper;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.RomeFeedParser;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Maximilian Irro
 */
@Service
public class ParserService {

    private final Logger log = LoggerFactory.getLogger(ParserService.class);

    private final String CATALOG_URL = "http://localhost:3031"; // TODO
    private final String INDEX_URL = "http://localhost:3032"; // TODO

    private final FeedParser feedParser = new RomeFeedParser();

    private final IndexMapper indexMapper = IndexMapper.INSTANCE;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void parseFeed(ParserJob job, Boolean isNewPodcast) {
        final String podcastExo = job.getExo();
        final String feedUrl = job.getUrl();
        final String feedData = job.getData();
        try {
            final Optional<PodcastDTO> podcast = Optional.ofNullable(feedParser.parseFeed(feedData));
            if (podcast.isPresent()) {

                final PodcastDTO p = podcast.get();
                p.setEchoId(podcastExo);

                Optional.ofNullable(p.getTitle()).ifPresent(t -> p.setTitle(t.trim()));
                Optional.ofNullable(p.getDescription()).ifPresent(d -> p.setDescription(Jsoup.clean(d, Whitelist.basic())));

                if (isNewPodcast) {
                    // TODO replace by sending job to queue
                    final String indexAddDocUrl = INDEX_URL+"/index/doc";
                    log.debug("Sending doc to index with request : {}", indexAddDocUrl);
                    final HttpEntity<IndexDocDTO> request = new HttpEntity<>(indexMapper.map(p));
                    restTemplate.postForEntity(indexAddDocUrl, request, IndexDocDTO.class);

                    if (!isNullOrEmpty(p.getLink())) {
                        // TODO tell crawler to download website content for podcast website
                    } else {
                        log.debug("No link set for podcast {} --> no website data will be added to the index", p.getEchoId());
                    }
                }

                // TODO replace by async job?
                // tell catalog to update podcast metadata
                final String catalogUpdateUrl = CATALOG_URL+"/catalog/podcast";
                log.debug("Sending podcast for update to catalog with request : {}", catalogUpdateUrl);
                restTemplate.put(catalogUpdateUrl, p);

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
        final EpisodeDTO[] episodes = feedParser.extractEpisodes(feedData);
        for (EpisodeDTO e : episodes) {

            Optional.ofNullable(e.getTitle()).ifPresent(t -> e.setTitle(t.trim()));
            Optional.ofNullable(e.getDescription()).ifPresent(d -> e.setDescription(Jsoup.clean(d, Whitelist.basic())));
            Optional.ofNullable(e.getContentEncoded()).ifPresent(c -> e.setContentEncoded(Jsoup.clean(c, Whitelist.basic())));

            // TODO replace by async job?
            // tell catalog to register episode if unknown
            final String catalogRegistrationUrl = CATALOG_URL+"/catalog/episode/register";
            log.debug("Sending episode for registration to catalog with request : {}", catalogRegistrationUrl);
            final EpisodeRegisterJob job = new EpisodeRegisterJob();
            job.setPodcastExo(podcastExo);
            job.setEpisode(e);
            final HttpEntity<EpisodeRegisterJob> request = new HttpEntity<>(job);
            restTemplate.postForEntity(catalogRegistrationUrl, request, EpisodeRegisterJob.class);
        }
    }

}
