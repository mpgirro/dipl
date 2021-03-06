package echo.core.benchmark.memory;

import echo.core.benchmark.BenchmarkMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class MemoryUsageMeter extends Thread implements BenchmarkMeter {

    private static final Logger log = LoggerFactory.getLogger(MemoryUsageMeter.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean measuring = new AtomicBoolean(false);
    private final List<Long> dataPoints = new LinkedList<>();
    private final String name;
    private final int interval;

    private MemoryUsageResult result;

    public MemoryUsageMeter(String name, int interval) {
        this.name = name;
        this.interval = interval;
        this.start();
    }

    @Override
    public void activate() {
        log.debug("{} : Activating the MemoryUsageMeter", name);
        running.set(true);
        this.start();
    }

    @Override
    public void deactivate() {
        log.debug("{} : Deactivating the MemoryUsageMeter", name);
        running.set(true);
    }

    @Override
    public void startMeasurement() {
        log.debug("{} : Starting the memory usage measurement", name);
        if (!isMeasuring()) {
            synchronized (dataPoints) {
                dataPoints.clear();
            }
        }
        measuring.set(true);
    }

    @Override
    public void stopMeasurement() {
        log.debug("{} : Stopping the memory usage measurement", name);
        if (isMeasuring()) {
            synchronized (dataPoints) {
                result = MemoryUsageResult.of(name, dataPoints);
            }
        }
        measuring.set(false);
    }

    @Override
    public boolean isActive() {
        return running.get();
    }

    @Override
    public boolean isMeasuring() {
        return measuring.get();
    }

    @Override
    public void run() {
        running.set(true);
        while (isActive()) {
            try {
                if (measuring.get()) {
                    synchronized (dataPoints) {
                        dataPoints.add(getRealMemoryUsage());
                    }
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("{} : Error on collecting datapoints : {}", name, e);
                e.printStackTrace();
            }
        }
    }

    public synchronized MemoryUsageResult getResult() {
        return Optional
            .ofNullable(result)
            .orElseThrow(() -> new RuntimeException(name + " : Memory usage result not yet available"));
    }

    private synchronized long getCurrentMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()
            + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    private synchronized long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    private synchronized long getRealMemoryUsage() {
        final long before = getGcCount();
        System.gc();
        while (getGcCount() == before); // busy waiting?!
        return getCurrentMemoryUsage();
    }

}
