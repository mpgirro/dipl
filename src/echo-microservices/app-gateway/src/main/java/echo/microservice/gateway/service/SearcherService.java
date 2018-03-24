package echo.microservice.gateway.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

/**
 * @author Maximilian Irro
 */
@Service
public class SearcherService {

    private final Logger log = LoggerFactory.getLogger(SearcherService.class);

    private final String SEARCHER_URL = "http://localhost:3035/searcher"; // TODO do not hardcode, use some sort of discovery mechanism

    private final RestTemplate restTemplate = new RestTemplate();

    @HystrixCommand(fallbackMethod = "fallbackSearch")
    public Optional<ResultWrapperDTO> search(String query, Integer page, Integer size) {
        log.debug("Request to search for query/page/size : ('{}',{},{})", query, page, size);

        String url = SEARCHER_URL+"/search?query="+query;
        if (page != null) url += "&page="+page;
        if (size != null) url += "&size="+size;

        //and do I need this JSON media type for my use case?
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<ResultWrapperDTO> entity = new HttpEntity<>(headers);

        final ResponseEntity<ResultWrapperDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, ResultWrapperDTO.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Optional.ofNullable(response.getBody());
        } else {
            log.warn("Got status from Catalog : {}", response.getStatusCode().value());
            return Optional.empty();
        }
    }

    /**
     * This methods produces the fallback search results, to be used if the Circuit Breaker
     * detects problems with the synchronous search calls. Fallback search result yields no
     * found entries (no podcasts, no episodes).
     *
     * <s>Note</s>: AspectJ weaving for the @HysterixCommand annotation requires this method to
     * have the same signature as the default search method. Fallback however does not make any
     * use of method parameters (query, page, size)
     *
     * @param query this parameter is unused for fallback result generation
     * @param page this parameter is unused for fallback result generation
     * @param size this parameter is unused for fallback result generation
     * @return fallback ResultWrapperDTO with no result entries
     */
    public Optional<ResultWrapperDTO> fallbackSearch(@SuppressWarnings("unused") String query,
                                                     @SuppressWarnings("unused") Integer page,
                                                     @SuppressWarnings("unused") Integer size) {
        log.warn("fallbackSearch has been invoked");

        return Optional.of(
            ImmutableResultWrapperDTO.builder()
                .setCurrPage(1)
                .setMaxPage(1)
                .setTotalHits(0)
                .setResults(Collections.emptyList())
                .create());
    }
}
