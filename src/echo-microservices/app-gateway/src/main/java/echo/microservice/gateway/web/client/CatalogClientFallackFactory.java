package echo.microservice.gateway.web.client;

import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.microservice.gateway.web.dto.ArrayWrapperDTO;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Component
public class CatalogClientFallackFactory implements FallbackFactory<CatalogClient> {
    private static final Logger log = LoggerFactory.getLogger(CatalogClientFallackFactory.class);

    @Override
    public CatalogClient create(Throwable cause) {
        return new CatalogClient() {

            @Override
            public PodcastDTO getPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return null; // the optional will be empty, and appropriate status code generated
            }

            @Override
            public ArrayWrapperDTO<PodcastDTO> getAllPodcasts(@SuppressWarnings("unused") Integer page,
                                                              @SuppressWarnings("unused") Integer size) {
                log.warn("getAllPodcasts fallback; reason was: {}, {}", cause.getMessage(), cause);
                return new ArrayWrapperDTO<>(Collections.emptyList()); // this list is immutable
            }

            @Override
            public EpisodeDTO getEpisode(@SuppressWarnings("unused") String exo) {
                log.warn("getEpisode fallback; reason was: {}, {}", cause.getMessage(), cause);
                return null; // the optional will be empty, and appropriate status code generated
            }

            @Override
            public ArrayWrapperDTO<EpisodeDTO> getEpisodesByPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getEpisodesByPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return new ArrayWrapperDTO<>(Collections.emptyList()); // this list is immutable
            }

            @Override
            public ArrayWrapperDTO<FeedDTO> getFeedsByPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getFeedsByPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return new ArrayWrapperDTO<>(Collections.emptyList()); // this list is immutable
            }

            @Override
            public ArrayWrapperDTO<ChapterDTO> getChaptersByEpisode(@SuppressWarnings("unused") String exo) {
                log.warn("getChaptersByEpisode fallback; reason was: {}, {}", cause.getMessage(), cause);
                return new ArrayWrapperDTO<>(Collections.emptyList()); // this list is immutable
            }
        };
    }
}
