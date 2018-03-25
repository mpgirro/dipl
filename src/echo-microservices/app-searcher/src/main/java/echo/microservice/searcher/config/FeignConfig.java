package echo.microservice.searcher.config;

import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"echo.microservice.searcher.web.client"})
@EnableCircuitBreaker
public class FeignConfig {
}
