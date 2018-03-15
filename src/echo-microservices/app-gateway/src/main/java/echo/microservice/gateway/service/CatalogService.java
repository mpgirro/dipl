package echo.microservice.gateway.service;

import com.google.common.collect.Lists;
import echo.core.domain.dto.ChapterDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.FeedDTO;
import echo.core.domain.dto.PodcastDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final String CATALOG_URL = "http://localhost:3031/catalog";

    private RestTemplate restTemplate = new RestTemplate();

    public Optional<PodcastDTO> getPodcast(String exo) {
        log.debug("Request to get Podcast (EXO) : {}", exo);

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
    }

    public List<PodcastDTO> getAllPodcasts(Integer page, Integer size) {
        log.debug("Request to get all Podcasts by page/size : ({},{})", page, size);

        String url = CATALOG_URL+"/podcast?";
        if (page != null) url += "page="+page;
        if (size != null) url += "size="+size;

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
    }

    public Optional<EpisodeDTO> getEpisode(String exo) {
        log.debug("Request to get Episode (EXO) : {}", exo);

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
    }

    public List<EpisodeDTO> getEpisodesByPodcast(String exo) {
        log.debug("Request to get Episodes by Podcast (EXO) : {}", exo);

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
    }

    public List<FeedDTO> getFeedsByPodcast(String exo) {
        log.debug("Request to get Feeds by Podcast (EXO) : {}", exo);

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
    }

    public List<ChapterDTO> getChaptersByEpisode(String exo) {
        log.debug("Request to get Chapters by Episode (EXO) : {}", exo);

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
    }

}
