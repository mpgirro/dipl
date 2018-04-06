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
    fallbackFactory = IndexClientFallbackFactory.class
)
public interface IndexClient {

    @GetMapping(value = "/index/search")
    ResultWrapperDTO getSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size);

}
