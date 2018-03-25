package echo.microservice.gateway.web.client;

import echo.core.domain.dto.ImmutableResultWrapperDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "searcher",
    url = "http://localhost:3035/searcher", // TODO obsolete once using registry service
    //fallback = SearcherClient.SearcherClientFallbackFactory.class,
    fallbackFactory = SearcherClientFallbackFactory.class
)
public interface SearcherClient {

    @GetMapping(value = "/search")
    ResultWrapperDTO getSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size);

}

@Component
class SearcherClientFallbackFactory implements FallbackFactory<SearcherClient> {
    private static final Logger log = LoggerFactory.getLogger(SearcherClientFallbackFactory.class);

    @Override
    public SearcherClient create(Throwable cause) {
        return new SearcherClient() {

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
            @Override
            public ResultWrapperDTO getSearchResults(@SuppressWarnings("unused") String query,
                                                     @SuppressWarnings("unused") Integer page,
                                                     @SuppressWarnings("unused") Integer size) {
                log.warn("fallback; reason was: {}, {}", cause.getMessage(), cause);

                if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
                    // Treat the HTTP 404 status
                    // TODO
                }

                log.warn("Exception: {}", cause.getMessage());
                cause.printStackTrace();

                return ImmutableResultWrapperDTO
                    .builder()
                    .setCurrPage(1)
                    .setMaxPage(1)
                    .setTotalHits(0)
                    .setResults(Collections.emptyList())
                    .create();
            }
        };
    }
}
