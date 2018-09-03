package echo.microservice.gateway.config;

import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
@EnableHystrix // or @EnableCircuitBreaker
@EnableHystrixDashboard
public class HysterixConfig {
}
