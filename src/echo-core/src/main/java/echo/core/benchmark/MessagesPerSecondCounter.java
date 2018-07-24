package echo.core.benchmark;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondCounter {

    private long startTimestamp;
    private long stopTimestamp;
    private long counter;

    public synchronized void startCounting() {
        counter = 0;
        startTimestamp = System.currentTimeMillis();
        stopTimestamp = 0; // to prevent premature calls to getMessagesPerSecond()
    }

    public synchronized void stopCounting() {
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
