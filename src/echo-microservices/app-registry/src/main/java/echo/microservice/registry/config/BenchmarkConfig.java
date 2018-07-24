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

    private final RoundTripTimeMonitor rttMonitor = new RoundTripTimeMonitor();
    private final MessagesPerSecondMonitor mpsMonitor = new MessagesPerSecondMonitor();

    @Bean(name = "roundTripTimeMonitor")
    public RoundTripTimeMonitor rttMonitor(){
        return rttMonitor;
    }

    @Bean(name = "messagesPerSecondMonitor")
    public MessagesPerSecondMonitor mpsMonitor(){
        return mpsMonitor;
    }

}
