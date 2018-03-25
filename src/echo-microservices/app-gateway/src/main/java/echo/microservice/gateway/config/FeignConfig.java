package echo.microservice.gateway.config;

import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableFeignClients(basePackages = {"echo.microservice.gateway.web.client"})
@EnableCircuitBreaker
public class FeignConfig {
}
