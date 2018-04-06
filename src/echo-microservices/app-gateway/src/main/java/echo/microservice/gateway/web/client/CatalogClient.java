package echo.microservice.gateway.web.client;

import echo.core.domain.dto.*;
import echo.microservice.gateway.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-catalog",
    configuration = FeignConfig.class,
    fallbackFactory = CatalogClientFallackFactory.class
)
public interface CatalogClient {

    @GetMapping(value = "/catalog/podcast/{exo}")
    PodcastDTO getPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/catalog/podcast/teaser")
    ArrayWrapperDTO<PodcastDTO> getAllPodcasts(@RequestParam("page") Integer page,
                                               @RequestParam("size") Integer size);

    @GetMapping(value = "/catalog/episode/{exo}")
    EpisodeDTO getEpisode(@PathVariable("exo") String exo);

    @GetMapping(value = "/catalog/podcast/{exo}/episodes")
    ArrayWrapperDTO<EpisodeDTO> getEpisodesByPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/catalog/podcast/{exo}/feeds")
    ArrayWrapperDTO<FeedDTO> getFeedsByPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/catalog/episode/{exo}/chapters")
    ArrayWrapperDTO<ChapterDTO> getChaptersByEpisode(@PathVariable("exo") String exo);

}
