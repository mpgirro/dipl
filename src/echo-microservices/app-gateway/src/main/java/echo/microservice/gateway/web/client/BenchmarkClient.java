package echo.microservice.gateway.web.client;

import echo.core.benchmark.RoundTripTime;
import echo.microservice.gateway.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-registry",
    url = "${feign.registry-url}",
    configuration = FeignConfig.class
)
public interface BenchmarkClient {

    @PostMapping(value = "/benchmark/rtt-report")
    void rttReport(@RequestBody RoundTripTime rtt);

    @PostMapping(value = "/benchmark/mps-report")
    void setMpsReport(@RequestParam("name") String name, @RequestParam("mps") Double mps);

}
