package echo.microservice.gateway.web.client;

import echo.core.domain.dto.*;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "searcher",
    url  = "${echo.gateway.catalog-url}", // TODO obsolete once using registry service
    fallbackFactory = CatalogClientFallackFactory.class
)
public interface CatalogClient {

    @GetMapping(value = "/podcast/{exo}")
    PodcastDTO getPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/podcast")
    ArrayWrapperDTO<PodcastDTO> getAllPodcasts(@RequestParam("page") Integer page, @RequestParam("size") Integer size);

    @GetMapping(value = "/episode/{exo}")
    EpisodeDTO getEpisode(@PathVariable("exo") String exo);

    @GetMapping(value = "/podcast/{exo}/episodes")
    ArrayWrapperDTO<EpisodeDTO> getEpisodesByPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/podcast/{exo}/feeds")
    ArrayWrapperDTO<FeedDTO> getFeedsByPodcast(@PathVariable("exo") String exo);

    @GetMapping(value = "/episode/{exo}/chapters")
    ArrayWrapperDTO<ChapterDTO> getChaptersByEpisode(@PathVariable("exo") String exo);

}
