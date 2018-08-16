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
    private final String name;

    private long startTime;
    private MessagesPerSecondResult result;

    public MessagesPerSecondMeter(String name) {
        this.name = name;
    }

    @Override
    public synchronized void activate() {
        log.debug("{} : Activating the MessagesPerSecondMeter", name);
        // Nothing to activate
    }

    @Override
    public synchronized void deactivate() {
        log.debug("{} : Deactivating the MessagesPerSecondMeter", name);
        // Nothing to deactivate
    }

    @Override
    public synchronized void startMeasurement() {
        log.debug("{} : Starting the MPS measurement", name);
        if (!isMeasuring()) {
            synchronized (buckets) {
                buckets.clear();
                startTime = System.currentTimeMillis() / 1000;
            }
        }
        measuring.set(true);
    }

    @Override
    public synchronized void stopMeasurement() {
        log.debug("{} : Stopping the MPS measurement", name);
        if (isMeasuring()) {
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

    public synchronized void registerMessage() {

        if (!isMeasuring()) {
            log.warn("{} : Cannot register new message - the meter is not measuring!", name);
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
            .orElseThrow(() -> new RuntimeException(name + " : Messages per second result not yet available"));
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
