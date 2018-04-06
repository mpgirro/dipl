package echo.microservice.searcher.web.client;

import echo.core.domain.dto.ResultWrapperDTO;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-index",
    //url = "${echo.gateway.index-url}/index", // TODO obsolete once using registry service
    fallbackFactory = IndexClientFallbackFactory.class
)
public interface IndexClient {

    @GetMapping(value = "/search")
    ResultWrapperDTO getSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size);

}
