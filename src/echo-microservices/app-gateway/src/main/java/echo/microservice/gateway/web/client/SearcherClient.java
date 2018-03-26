package echo.microservice.gateway.web.client;

import echo.core.domain.dto.ResultWrapperDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "searcher",
    url = "http://localhost:3035/searcher", // TODO obsolete once using registry service
    fallbackFactory = SearcherClientFallbackFactory.class
)
public interface SearcherClient {

    @GetMapping(value = "/search")
    ResultWrapperDTO getSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size);

}
