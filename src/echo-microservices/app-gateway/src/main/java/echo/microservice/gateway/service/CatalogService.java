package echo.microservice.gateway.service;

import com.google.common.collect.Lists;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import echo.core.domain.dto.*;
import echo.microservice.gateway.web.client.CatalogClient;
import echo.microservice.gateway.web.dto.ArrayWrapperDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class CatalogService {

    private final Logger log = LoggerFactory.getLogger(CatalogService.class);

    @Value("${echo.gateway.fallback-title:Uh oh}")
    private String FALLBACK_TITLE;

    @Value("${echo.gateway.fallback-description:Data could not be loaded due to a temporary problem. Try again later}")
    private String FALLBACK_DESCRIPTION;

    @Autowired
    private CatalogClient catalogClient;

    private final String CATALOG_URL = "http://localhost:3031/catalog"; // TODO do not hardcode, use some sort of discovery mechanism

    private final RestTemplate restTemplate = new RestTemplate();

    //@HystrixCommand(fallbackMethod = "fallbackGetPodcast")
    public Optional<PodcastDTO> getPodcast(String exo) {
        log.debug("Request to get Podcast (EXO) : {}", exo);

        /* TODO delete?
        final String url = CATALOG_URL+"/podcast/" + exo;

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<PodcastDTO> entity = new HttpEntity<>(headers);

        final ResponseEntity<PodcastDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, PodcastDTO.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Optional.empty();
        }
        */

        final PodcastDTO response = catalogClient.getPodcast(exo);
        return Optional.ofNullable(response);
    }

    //@HystrixCommand(fallbackMethod = "fallbackGetAllPodcasts")
    public List<PodcastDTO> getAllPodcasts(Integer page, Integer size) {
        log.debug("Request to get all Podcasts by page/size : ({},{})", page, size);

        /*
        String url = CATALOG_URL+"/podcast?";
        if (page != null) url += "page="+page;
        if (size != null) url += "&size="+size;

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<PodcastDTO[]> entity = new HttpEntity<>(headers);

        final ResponseEntity<PodcastDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, PodcastDTO[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Lists.newArrayList(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Collections.emptyList();
        }
        */

        final ArrayWrapperDTO<PodcastDTO> wrapper = catalogClient.getAllPodcasts(page, size);

        log.debug("Received all podcasts from catalog : {}", wrapper);

        return wrapper.getResults();
    }

    //@HystrixCommand(fallbackMethod = "fallbackGetEpisode")
    public Optional<EpisodeDTO> getEpisode(String exo) {
        log.debug("Request to get Episode (EXO) : {}", exo);

        /*
        final String url = CATALOG_URL+"/episode/" + exo;

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<EpisodeDTO> entity = new HttpEntity<>(headers);

        final ResponseEntity<EpisodeDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, EpisodeDTO.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Optional.empty();
        }
        */
        final EpisodeDTO response = catalogClient.getEpisode(exo);
        return Optional.ofNullable(response);
    }

    //@HystrixCommand(fallbackMethod = "fallbackGetEpisodesByPodcast")
    public List<EpisodeDTO> getEpisodesByPodcast(String exo) {
        log.debug("Request to get Episodes by Podcast (EXO) : {}", exo);

        /*
        final String url = CATALOG_URL+"/podcast/"+exo+"/episodes";

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<EpisodeDTO[]> entity = new HttpEntity<>(headers);

        final ResponseEntity<EpisodeDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, EpisodeDTO[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Lists.newArrayList(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Collections.emptyList();
        }
        */
        return catalogClient.getEpisodesByPodcast(exo).getResults();
    }

    //@HystrixCommand(fallbackMethod = "fallbackGetFeedsByPodcast")
    public List<FeedDTO> getFeedsByPodcast(String exo) {
        log.debug("Request to get Feeds by Podcast (EXO) : {}", exo);

        /*
        final String url = CATALOG_URL+"/podcast/"+exo+"/feeds";

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<FeedDTO[]> entity = new HttpEntity<>(headers);

        final ResponseEntity<FeedDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, FeedDTO[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Lists.newArrayList(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Collections.emptyList();
        }
        */
        return catalogClient.getFeedsByPodcast(exo).getResults();
    }

    //@HystrixCommand(fallbackMethod = "fallbackGetChaptersByEpisode")
    public List<ChapterDTO> getChaptersByEpisode(String exo) {
        log.debug("Request to get Chapters by Episode (EXO) : {}", exo);

        /*
        final String url = CATALOG_URL+"/episode/"+exo+"/chapters";

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<ChapterDTO[]> entity = new HttpEntity<>(headers);

        final ResponseEntity<ChapterDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, ChapterDTO[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Lists.newArrayList(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Collections.emptyList();
        }
        */
        return catalogClient.getChaptersByEpisode(exo).getResults();
    }

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
    @Deprecated
    public Optional<PodcastDTO> fallbackGetPodcast(@SuppressWarnings("unused") String exo) {
        log.warn("fallbackGetPodcast has been invoked");

        return Optional.of(
            ImmutablePodcastDTO.builder()
                .setEchoId(exo)
                .setTitle(FALLBACK_TITLE)
                .setDescription(FALLBACK_DESCRIPTION)
                .create());
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
    @Deprecated
    public List<PodcastDTO> fallbackGetAllPodcasts(@SuppressWarnings("unused") Integer page,
                                                    @SuppressWarnings("unused") Integer size) {
        log.warn("fallbackGetAllPodcasts has been invoked");
        return Collections.emptyList(); // this list is immutable
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
    @Deprecated
    public Optional<EpisodeDTO> fallbackGetEpisode(@SuppressWarnings("unused") String exo) {
        log.warn("fallbackGetEpisode has been invoked");

        return Optional.of(
            ImmutableEpisodeDTO.builder()
                .setEchoId(exo)
                .setTitle(FALLBACK_TITLE)
                .setDescription(FALLBACK_DESCRIPTION)
                .create());
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
    @Deprecated
    public List<EpisodeDTO> fallbackGetEpisodesByPodcast(@SuppressWarnings("unused") String exo) {
        log.warn("fallbackGetEpisodesByPodcast has been invoked");
        return Collections.emptyList(); // this list is immutable
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
    @Deprecated
    public List<FeedDTO> fallbackGetFeedsByPodcast(@SuppressWarnings("unused") String exo) {
        log.warn("fallbackGetFeedsByPodcast has been invoked");
        return Collections.emptyList(); // this list is immutable
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
    @Deprecated
    public List<ChapterDTO> fallbackGetChaptersByEpisode(@SuppressWarnings("unused") String exo) {
        log.warn("fallbackGetChaptersByEpisode has been invoked");
        return Collections.emptyList(); // this list is immutable
    }


}
