package echo.microservice.registry.config;

import echo.core.benchmark.MessagesPerSecondCounter;
import echo.core.benchmark.MessagesPerSecondMonitor;
import echo.core.benchmark.RoundTripTimeMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    @Bean(name = "roundTripTimeMonitor")
    public RoundTripTimeMonitor rttMonitor(){
        return new RoundTripTimeMonitor();
    }

    @Bean(name = "messagesPerSecondMonitor")
    public MessagesPerSecondMonitor mpsMonitor(){
        return new MessagesPerSecondMonitor();
    }

}
