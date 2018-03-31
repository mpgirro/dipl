package echo.microservice.gateway.web.client;

import echo.core.domain.dto.*;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author Maximilian Irro
 */
@Component
public class CatalogClientFallackFactory implements FallbackFactory<CatalogClient> {
    private static final Logger log = LoggerFactory.getLogger(CatalogClientFallackFactory.class);

    @Override
    public CatalogClient create(Throwable cause) {
        return new CatalogClient() {

            /**
             * This methods produces the fallback result, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method
             * to have the same signature as the default method. Fallback however does not make any
             * use of method parameters.
             *
             * @param exo this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public PodcastDTO getPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return null; // the optional will be empty, and appropriate status code generated
            }

            /**
             * This methods produces the fallback results, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method
             * to have the same signature as the default method. Fallback however does not make any
             * use of method parameters.
             *
             * @param page this parameter is unused for fallback result generation
             * @param size this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public ArrayWrapperDTO<PodcastDTO> getAllPodcasts(@SuppressWarnings("unused") Integer page,
                                                              @SuppressWarnings("unused") Integer size) {
                log.warn("getAllPodcasts fallback; reason was: {}, {}", cause.getMessage(), cause);
                return ImmutableArrayWrapperDTO.of(Collections.emptyList()); // emptyList() produces an immutable list
            }

            /**
             * This methods produces the fallback result, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method to
             * have the same signature as the default search method. Fallback however does not make any
             * use of method parameters (query, page, size)
             *
             * @param exo this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public EpisodeDTO getEpisode(@SuppressWarnings("unused") String exo) {
                log.warn("getEpisode fallback; reason was: {}, {}", cause.getMessage(), cause);
                return null; // the optional will be empty, and appropriate status code generated
            }

            /**
             * This methods produces the fallback results, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method
             * to have the same signature as the default method. Fallback however does not make any
             * use of method parameters.
             *
             * @param exo this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public ArrayWrapperDTO<EpisodeDTO> getEpisodesByPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getEpisodesByPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return ImmutableArrayWrapperDTO.of(Collections.emptyList()); // emptyList() produces an immutable list
            }

            /**
             * This methods produces the fallback results, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method
             * to have the same signature as the default method. Fallback however does not make any
             * use of method parameters.
             *
             * @param exo this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public ArrayWrapperDTO<FeedDTO> getFeedsByPodcast(@SuppressWarnings("unused") String exo) {
                log.warn("getFeedsByPodcast fallback; reason was: {}, {}", cause.getMessage(), cause);
                return ImmutableArrayWrapperDTO.of(Collections.emptyList()); // emptyList() produces an immutable list
            }

            /**
             * This methods produces the fallback results, to be used if the Circuit Breaker
             * detects problems with the synchronous calls to the catalog.
             *
             * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method
             * to have the same signature as the default method. Fallback however does not make any
             * use of method parameters.
             *
             * @param exo this parameter is unused for fallback result generation
             * @return fallback PodcastDTO with just some information that data could not be loaded
             */
            @Override
            public ArrayWrapperDTO<ChapterDTO> getChaptersByEpisode(@SuppressWarnings("unused") String exo) {
                log.warn("getChaptersByEpisode fallback; reason was: {}, {}", cause.getMessage(), cause);
                return ImmutableArrayWrapperDTO.of(Collections.emptyList()); // emptyList() produces an immutable list
            }
        };
    }
}
