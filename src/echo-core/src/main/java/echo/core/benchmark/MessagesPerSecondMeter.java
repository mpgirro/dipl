package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondMeter implements BenchmarkMeter {

    private static final Logger log = LoggerFactory.getLogger(MessagesPerSecondMeter.class);

    private long startTimestamp;
    private long stopTimestamp;
    private long counter;

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
        counter = 0;
        startTimestamp = System.currentTimeMillis();
        stopTimestamp = 0; // to prevent premature calls to getMessagesPerSecond()
    }

    @Override
    public synchronized void stopMeasurement() {
        log.debug("Stopping the MPS measurement");
        stopTimestamp = System.currentTimeMillis();
    }

    public synchronized void incrementCounter() {
        counter += 1;
    }

    public synchronized double getMessagesPerSecond() {
        final long elaspedTime = stopTimestamp - startTimestamp;
        if (elaspedTime > 0 && counter > 0) {
            final double c = (double) counter;
            final double t = ((double) elaspedTime) / 1000;
            return c / t;
        } else {
            return 0;
        }
    }

}
