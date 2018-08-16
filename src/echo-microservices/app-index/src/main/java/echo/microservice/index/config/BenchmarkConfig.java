package echo.microservice.index.config;

import echo.core.benchmark.cpu.CpuLoadMeter;
import echo.core.benchmark.memory.MemoryUsageMeter;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public BenchmarkConfig(@Value("${spring.application.name:echo-index}") String applicationName, @Value("${echo.benchmark.meter-interval:200}") Integer interval) {
        messagesPerSecondMeter = new MessagesPerSecondMeter(applicationName);
        memoryUsageMeter = new MemoryUsageMeter(applicationName, interval);
        cpuLoadMeter = new CpuLoadMeter(applicationName, interval);
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
