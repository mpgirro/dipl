package echo.microservice.crawler.config;

import echo.core.benchmark.MessagesPerSecondCounter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    private final MessagesPerSecondCounter mpsCounter = new MessagesPerSecondCounter();

    @Bean(name = "messagesPerSecondCounter")
    public MessagesPerSecondCounter mpsCounter(){
        return mpsCounter;
    }

}
