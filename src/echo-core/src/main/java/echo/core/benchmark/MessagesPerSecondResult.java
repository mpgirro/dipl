package echo.core.benchmark;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondResult {

    public final long startTime;
    public final long stopTime;
    public final double mps;
    public final String mpsStr;

    private MessagesPerSecondResult(long startTime, long stopTime, long counter) {
        this.startTime = startTime;
        this.stopTime = stopTime;

        final long elaspedTime = stopTime - startTime;
        if (elaspedTime > 0 && counter > 0) {
            final double c = (double) counter;
            final double t = ((double) elaspedTime) / 1000;
            mps = c / t;
        } else {
            mps = 0.0;
        }
        mpsStr = "" + ((double) Math.round(mps * 100) / 100);
    }

    public static MessagesPerSecondResult of(long startTime, long stopTime, long counter) {
        return new MessagesPerSecondResult(startTime, stopTime, counter);
    }

    /* TODO
    public double getMps() {
        return mps;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }
    */

}
