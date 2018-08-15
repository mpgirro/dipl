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
    private final int interval;
    private final List<Long> dataPoints = new LinkedList<>();

    private MemoryUsageResult result;

    public MemoryUsageMeter(int interval) {
        this.interval = interval;
        this.start();
    }

    @Override
    public void activate() {
        log.debug("Activating the MemoryUsageMeter");
        running.set(true);
        this.start();
    }

    @Override
    public void deactivate() {
        log.debug("Deactivating the MemoryUsageMeter");
        running.set(true);
    }

    @Override
    public void startMeasurement() {
        log.debug("Starting the memory usage measurement");
        if (!measuring.get()) {
            synchronized (dataPoints) {
                dataPoints.clear();
            }
        }
        measuring.set(true);
    }

    @Override
    public void stopMeasurement() {
        log.debug("Stopping the memory usage measurement");
        if (measuring.get()) {
            synchronized (dataPoints) {
                result = MemoryUsageResult.of(dataPoints);
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
        while (running.get()) {
            try {
                if (measuring.get()) {
                    synchronized (dataPoints) {
                        dataPoints.add(getRealMemoryUsage());
                    }
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("Error on collecting datapoints : {}", e);
                e.printStackTrace();
            }
        }
    }

    public synchronized MemoryUsageResult getResult() {
        return Optional
            .ofNullable(result)
            .orElseThrow(() -> new RuntimeException("Memory usage result not yet available"));
    }

    /* TODO delete?
    public List<Long> getDataPoints() {
        synchronized (dataPoints) {
            return dataPoints;
        }
    }

    public synchronized double getMeanMemoryUsage() {
        double result = 0;
        synchronized (dataPoints) {
            if (dataPoints.size() > 0) {
                final long sum = dataPoints.stream()
                    .mapToLong(Long::longValue)
                    .sum();

                result = ((double) sum) / dataPoints.size();
            }
        }
        return result;
    }

    private long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public synchronized String getMeanMemoryUsageStr() {
        return bytesToMegabytes((long) getMeanMemoryUsage()) + " MB";
    }

    */

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
