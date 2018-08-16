package echo.microservice.crawler.web.client;

import echo.core.benchmark.BenchmarkMeterReport;
import echo.core.benchmark.cpu.CpuLoadResult;
import echo.core.benchmark.memory.MemoryUsageResult;
import echo.core.benchmark.mps.MessagesPerSecondResult;
import echo.microservice.crawler.config.FeignConfig;
import feign.Headers;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Maximilian Irro
 */
@FeignClient(
    name = "echo-registry",
    url = "${feign.registry-url}",
    configuration = FeignConfig.class
)
public interface BenchmarkClient {

    @PostMapping(value = "/benchmark/mps-report")
    void sendMpsReport(@RequestParam("name") String name, @RequestBody MessagesPerSecondResult messagesPerSecondResult);

    @PostMapping(value = "/benchmark/cpu-report")
    void sendCpuReport(@RequestParam("name") String name, @RequestBody CpuLoadResult cpuLoadResult);

    @PostMapping(value = "/benchmark/memory-report")
    void sendMemoryReport(@RequestParam("name") String name, @RequestBody MemoryUsageResult memoryUsageResult);

    //@PostMapping(value = "/benchmark/benchmark-report")
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/benchmark/benchmark-report",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: "+ MediaType.APPLICATION_JSON_VALUE)
    void sendBenchmarkReport(@RequestBody BenchmarkMeterReport report);

}
