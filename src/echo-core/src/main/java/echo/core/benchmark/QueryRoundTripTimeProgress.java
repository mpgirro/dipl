package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maximilian Irro
 */
public class QueryRoundTripTimeProgress extends RoundTripTimeProgress {

    private static final Logger log = LoggerFactory.getLogger(QueryRoundTripTimeProgress.class);

    private RoundTripTime resultsRTT;

    public QueryRoundTripTimeProgress(String id) {
        super(id);
    }

    @Override
    public boolean finished() {
        return (resultsRTT!=null);
    }

    @Override
    public void calculateEvaluation() {
        firstTimestamp = resultsRTT.getRtts().get(0);
        lastTimestamp = resultsRTT.getRtts().get(resultsRTT.getRtts().size()-1);

        meanRoundTripTime = lastTimestamp - firstTimestamp; // only one RTT here
        meanMessageLatency = resultsRTT.getMeanMessageLatency();
    }

    public void setResultsRTT(RoundTripTime resultsRTT) {
        this.resultsRTT = resultsRTT;
    }
}
