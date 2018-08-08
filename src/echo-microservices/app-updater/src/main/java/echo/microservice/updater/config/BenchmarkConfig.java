package echo.microservice.updater.config;

import echo.core.benchmark.MessagesPerSecondMeter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    private final MessagesPerSecondMeter mpsMeter = new MessagesPerSecondMeter();

    @Bean(name = "messagesPerSecondMeter")
    public MessagesPerSecondMeter mpsMeter(){
        return mpsMeter;
    }

}
