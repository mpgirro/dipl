package echo.microservice.index.service;

import echo.core.benchmark.BenchmarkMeterReport;
import echo.core.benchmark.ImmutableBenchmarkMeterReport;
import echo.core.benchmark.cpu.CpuLoadMeter;
import echo.core.benchmark.memory.MemoryUsageMeter;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.microservice.index.web.client.BenchmarkClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Maximilian Irro
 */
@Service
public class BenchmarkService {

    private final Logger log = LoggerFactory.getLogger(BenchmarkService.class);

    @Value("${spring.application.name:echo-catalog}")
    private String applicationName;

    @Autowired
    private BenchmarkClient benchmarkClient;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @Resource(name = "memoryUsageMeter")
    private MemoryUsageMeter memoryUsageMeter;

    @Resource(name = "cpuLoadMeter")
    private CpuLoadMeter cpuLoadMeter;

    @Async
    public void startBenchmarkMeters() {
        mpsMeter.startMeasurement();
        memoryUsageMeter.startMeasurement();
        cpuLoadMeter.startMeasurement();
    }

    @Async
    public void stopBenchmarkMetersAndSendReport() {
        mpsMeter.stopMeasurement();
        memoryUsageMeter.stopMeasurement();
        cpuLoadMeter.stopMeasurement();

        final BenchmarkMeterReport report = ImmutableBenchmarkMeterReport.builder()
            .setName(applicationName)
            .setMps(mpsMeter.getResult())
            .setMemoryUsage(memoryUsageMeter.getResult())
            .setCpuLoad(cpuLoadMeter.getResult())
            .create();

        log.info("Sending benchmark report");
        log.debug("{}", report);
        benchmarkClient.sendBenchmarkReport(report);
    }

    @Async
    public void sendRttReport(RoundTripTime rtt) {
        try {
            benchmarkClient.sendRttReport(rtt);
        } catch (FeignException e) {
            log.error("Error sending RTT : {} ; Reason: {}", rtt, e);
        }

    }

}
