package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class MemoryUsageMeter extends Thread {

    private static final Logger log = LoggerFactory.getLogger(MemoryUsageMeter.class);

    private static final long MEGABYTE = 1000L * 1000L;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private final int interval;

    private final List<Long> dataPoints = new LinkedList<>();

    public MemoryUsageMeter(int interval) {
        this.interval = interval;
    }

    public void startMonitoring() {
        log.debug("Starting the monitoring");
        monitoring.set(true);
        synchronized (dataPoints) {
            dataPoints.clear();
        }
    }

    public void stopMonitoring() {
        log.debug("Stopping the monitoring");
        monitoring.set(false);
    }

    public void halt() {
        log.debug("Halting the meter");
        running.set(false);
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                if (monitoring.get()) {
                    synchronized (dataPoints) {
                        dataPoints.add(getRealMemoryUsage());
                    }
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized long getCurrentMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()
            + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    public synchronized long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    public synchronized long getRealMemoryUsage() {
        final long before = getGcCount();
        System.gc();
        while (getGcCount() == before); // busy waiting?!
        return getCurrentMemoryUsage();
    }

    public synchronized double getMeanMemoryUsage() {
        double result = 0;
        synchronized (dataPoints) {
            if (dataPoints.size() > 0) {
                final long sum = dataPoints.stream()
                    .mapToLong(Long::longValue)
                    .sum();

                result = ((double) sum) / dataPoints.size();
                log.debug("((double) {}) / {} = {}", (double) sum, dataPoints.size(), result);
            }
        }
        return result;
    }

    public synchronized String getMeanMemoryUsageStr() {
        return bytesToMegabytes((long) getMeanMemoryUsage()) + " MB";
    }

    public synchronized List<Long> getDataPoints() {
        synchronized (dataPoints) {
            return dataPoints;
        }
    }



    private long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

}
