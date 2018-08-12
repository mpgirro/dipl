package echo.microservice.index.web.client;

import echo.core.benchmark.rtt.RoundTripTime;
import echo.microservice.index.config.FeignConfig;
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
    void mpsReport(@RequestParam("name") String name, @RequestParam("mps") Double mps);

}
