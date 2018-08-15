package echo.core.benchmark.mps;

import echo.core.benchmark.BenchmarkMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondMeter implements BenchmarkMeter {

    private static final Logger log = LoggerFactory.getLogger(MessagesPerSecondMeter.class);

    private final AtomicBoolean measuring = new AtomicBoolean(false);
    private final Map<Long, Long> buckets = new HashMap<>();

    private long startTime;
    private MessagesPerSecondResult result;

    @Override
    public synchronized void activate() {
        log.debug("Activating the MessagesPerSecondMeter");
        // Nothing to activate
    }

    @Override
    public synchronized void deactivate() {
        log.debug("Deactivating the MessagesPerSecondMeter");
        // Nothing to deactivate
    }

    @Override
    public synchronized void startMeasurement() {
        log.debug("Starting the MPS measurement");
        if (!measuring.get()) {
            synchronized (buckets) {
                buckets.clear();
                startTime = System.currentTimeMillis() / 1000;
            }
        }
        measuring.set(true);
    }

    @Override
    public synchronized void stopMeasurement() {
        log.debug("Stopping the MPS measurement");
        if (measuring.get()) {
            synchronized (buckets) {
                result = MessagesPerSecondResult.of(buckets);
            }
        }
        measuring.set(false);
    }

    @Override
    public boolean isActive() {
        return true; // always active (no thread)
    }

    @Override
    public boolean isMeasuring() {
        return measuring.get();
    }

    public void registerMessage() {

        if (!measuring.get()) {
            log.warn("Cannot register new message - the meter is not running!");
            return;
        }

        synchronized (buckets) {
            final long currentSec = System.currentTimeMillis() / 1000; // in seconds
            final long b = currentSec - startTime;
            if (buckets.containsKey(b)) {
                buckets.put(b, buckets.get(b)+1);
            } else {
                buckets.put(b, 1L);
            }
        }
    }

    public synchronized MessagesPerSecondResult getResult() {
        return Optional
            .ofNullable(result)
            .orElseThrow(() -> new RuntimeException("Messages per second result not yet available"));
    }

    /*
    public synchronized double getMessagesPerSecond() {
        final long elaspedTime = stopTime - startTime;
        if (elaspedTime > 0 && counter > 0) {
            final double c = (double) counter;
            final double t = ((double) elaspedTime) / 1000;
            return c / t;
        } else {
            return 0;
        }
    }
    */

}
