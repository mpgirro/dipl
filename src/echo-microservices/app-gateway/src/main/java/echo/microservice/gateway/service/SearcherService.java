package echo.microservice.gateway.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.gateway.web.client.SearcherClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${echo.gateway.default-page:1}")
    private Integer DEFAULT_PAGE;

    @Value("${echo.gateway.default-size:20}")
    private Integer DEFAULT_SIZE;

    @Autowired
    private SearcherClient searcherClient;

    public Optional<ResultWrapperDTO> search(String query, Optional<Integer> page, Optional<Integer> size) {
        log.debug("Request to search for query/page/size : ('{}',{},{})", query, page, size);

        // TODO check if page < 0 and fail
        // TODO check of size < 0 and fail
        // TODO do some other facy searcher stuff

        final Integer p = page.orElse(DEFAULT_PAGE);
        final Integer s = size.orElse(DEFAULT_SIZE);

        final ResultWrapperDTO result = searcherClient.getSearchResults(query, p, s);

        return Optional.of(result);
    }

    // TODO delete
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
    @Deprecated
    public Optional<ResultWrapperDTO> fallbackSearch(@SuppressWarnings("unused") String query,
                                                     @SuppressWarnings("unused") Optional<Integer> page,
                                                     @SuppressWarnings("unused") Optional<Integer> size) {
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
