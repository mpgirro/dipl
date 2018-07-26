package echo.core.benchmark;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    @Override
    public List<RoundTripTime> getAllRTTs() {
        return ImmutableList.of(resultsRTT);
    }

    public void setResultsRTT(RoundTripTime resultsRTT) {
        this.resultsRTT = resultsRTT;
    }
}
