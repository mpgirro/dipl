package echo.microservice.catalog.config;

import echo.core.benchmark.CpuLoadMeter;
import echo.core.benchmark.MemoryUsageMeter;
import echo.core.benchmark.MessagesPerSecondMeter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Maximilian Irro
 */
@Configuration
public class BenchmarkConfig {

    private final MessagesPerSecondMeter messagesPerSecondMeter;
    private final MemoryUsageMeter memoryUsageMeter;
    private final CpuLoadMeter cpuLoadMeter;

    @Autowired
    public BenchmarkConfig(@Value("${echo.benchmark.meter-interval:200}") Integer interval) {
        messagesPerSecondMeter = new MessagesPerSecondMeter();
        memoryUsageMeter = new MemoryUsageMeter(interval);
        cpuLoadMeter = new CpuLoadMeter(interval);
    }

    @PreDestroy
    public void destroy() {
        memoryUsageMeter.deactivate();
        cpuLoadMeter.deactivate();
    }

    @Bean(name = "messagesPerSecondMeter")
    public MessagesPerSecondMeter mpsMeter(){
        return messagesPerSecondMeter;
    }

    @Bean(name = "memoryUsageMeter")
    public MemoryUsageMeter memoryUsageMeter(){
        return memoryUsageMeter;
    }

    @Bean(name = "cpuLoadMeter")
    public CpuLoadMeter cpuLoadMeter(){
        return cpuLoadMeter;
    }

}
