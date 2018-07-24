package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public abstract class RoundTripTimeProgress {

    private static final Logger log = LoggerFactory.getLogger(RoundTripTimeProgress.class);

    private final String id; // URL or query

    protected long firstTimestamp;
    protected long lastTimestamp;
    protected long meanRoundTripTime;
    protected double meanMessageLatency;

    public RoundTripTimeProgress(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract boolean finished();

    public abstract void calculateEvaluation();

    public long getOverallRoundTripTime() {
        return lastTimestamp - firstTimestamp;
    }

    public long getMeanRoundTripTime() {
        return meanRoundTripTime;
    }

    public double getMeanMessageLatency() {
        return meanMessageLatency;
    }

}