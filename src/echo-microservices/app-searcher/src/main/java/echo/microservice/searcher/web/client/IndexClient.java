package echo.microservice.searcher.web.client;

import echo.core.benchmark.rtt.RoundTripTime;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.microservice.searcher.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-index",
    url = "${feign.index-url}",
    configuration = FeignConfig.class,
    fallbackFactory = IndexClientFallbackFactory.class
)
public interface IndexClient {

    @GetMapping(value = "/index/search")
    ResultWrapperDTO getSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size);

    @PostMapping(value = "/benchmark/search")
    ResultWrapperDTO getBenchmarkSearchResults(@RequestParam("query") String query,
                                      @RequestParam("page") Integer page,
                                      @RequestParam("size") Integer size,
                                      @RequestBody RoundTripTime rtt);

}
