package echo.core.benchmark.mps;

import echo.core.benchmark.BenchmarkMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondMeter implements BenchmarkMeter {

    private static final Logger log = LoggerFactory.getLogger(MessagesPerSecondMeter.class);

    private final AtomicBoolean measuring = new AtomicBoolean(false);

    private long startTime;
    private long stopTime;
    private long counter;

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
            counter = 0;
            startTime = System.currentTimeMillis();
            stopTime = 0; // to prevent premature calls to getMessagesPerSecond()
        }
        measuring.set(true);
    }

    @Override
    public synchronized void stopMeasurement() {
        log.debug("Stopping the MPS measurement");
        if (measuring.get()) {
            stopTime = System.currentTimeMillis();
            calculateResult();
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

    public synchronized void incrementCounter() {
        counter += 1;
    }

    public synchronized MessagesPerSecondResult getResult() {
        return Optional
            .ofNullable(result)
            .orElseThrow(() -> new RuntimeException("Messages per second result not yet available"));
    }

    private void calculateResult() {
        result = MessagesPerSecondResult.of(startTime, stopTime, counter);
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
