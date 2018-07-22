package echo.microservice.index.config;

import echo.core.benchmark.MessagesPerSecondCounter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    @Bean(name = "messagesPerSecondCounter")
    public MessagesPerSecondCounter mpsCounter(){
        return new MessagesPerSecondCounter();
    }


}
