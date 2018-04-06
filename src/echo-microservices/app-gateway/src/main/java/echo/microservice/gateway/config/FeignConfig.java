package echo.microservice.gateway.config;

import feign.Logger;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"echo.microservice.gateway.web.client"})
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

}
