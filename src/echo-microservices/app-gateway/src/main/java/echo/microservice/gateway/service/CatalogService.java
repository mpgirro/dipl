package echo.microservice.gateway.service;

import echo.core.domain.dto.*;
import echo.microservice.gateway.web.client.CatalogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class CatalogService {

    private final Logger log = LoggerFactory.getLogger(CatalogService.class);

    @Value("${echo.gateway.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.gateway.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Value("${echo.gateway.fallback-title:Uh oh}")
    private String FALLBACK_TITLE;

    @Value("${echo.gateway.fallback-description:Data could not be loaded due to a temporary problem. Try again later}")
    private String FALLBACK_DESCRIPTION;

    @Autowired
    private CatalogClient catalogClient;

    public Optional<PodcastDTO> getPodcast(String exo) {
        log.debug("Request to get Podcast (EXO) : {}", exo);
        final PodcastDTO response = catalogClient.getPodcast(exo);
        return Optional.ofNullable(response);
    }

    public List<PodcastDTO> getAllPodcasts(Optional<Integer> page, Optional<Integer> size) {
        log.debug("Request to get all Podcasts by page/size : ({},{})", page, size);

        final Integer p = page.orElse(DEFAULT_PAGE);
        final Integer s = size.orElse(DEFAULT_SIZE);

        final ArrayWrapperDTO<PodcastDTO> wrapper = catalogClient.getAllPodcasts(p, s);

        log.debug("Received all podcasts from catalog : {}", wrapper);

        return wrapper.getResults();
    }

    public Optional<EpisodeDTO> getEpisode(String exo) {
        log.debug("Request to get Episode (EXO) : {}", exo);
        final EpisodeDTO response = catalogClient.getEpisode(exo);
        return Optional.ofNullable(response);
    }

    public List<EpisodeDTO> getEpisodesByPodcast(String exo) {
        log.debug("Request to get Episodes by Podcast (EXO) : {}", exo);
        return catalogClient.getEpisodesByPodcast(exo).getResults();
    }

    public List<FeedDTO> getFeedsByPodcast(String exo) {
        log.debug("Request to get Feeds by Podcast (EXO) : {}", exo);
        return catalogClient.getFeedsByPodcast(exo).getResults();
    }

    public List<ChapterDTO> getChaptersByEpisode(String exo) {
        log.debug("Request to get Chapters by Episode (EXO) : {}", exo);
        return catalogClient.getChaptersByEpisode(exo).getResults();
    }

}
