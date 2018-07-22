package echo.microservice.catalog.web.client;

import echo.microservice.catalog.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-registry",
    configuration = FeignConfig.class
)
public interface BenchmarkClient {

    @PostMapping(value = "/benchmark/mps-report")
    void setMpsReport(@RequestParam("name") String name, @RequestParam("mps") Double mps);

}
