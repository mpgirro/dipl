package echo.microservice.registry.config;

import echo.core.benchmark.ArchitectureType;
import echo.core.benchmark.mps.MessagesPerSecondMonitor;
import echo.core.benchmark.rtt.RoundTripTimeMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    private final RoundTripTimeMonitor rttMonitor = new RoundTripTimeMonitor(ArchitectureType.ECHO_MSA);
    private final MessagesPerSecondMonitor mpsMonitor = new MessagesPerSecondMonitor(ArchitectureType.ECHO_MSA,7); // 'cause we have 7 MS in place that will report their MPS

    @Bean(name = "roundTripTimeMonitor")
    public RoundTripTimeMonitor rttMonitor(){
        return rttMonitor;
    }

    @Bean(name = "messagesPerSecondMonitor")
    public MessagesPerSecondMonitor mpsMonitor(){
        return mpsMonitor;
    }

}
