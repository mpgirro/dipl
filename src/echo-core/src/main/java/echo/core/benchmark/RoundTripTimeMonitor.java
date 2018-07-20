package echo.core.benchmark;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Maximilian Irro
 */
public class RoundTripTimeMonitor {

    private static final Logger log = LoggerFactory.getLogger(RoundTripTimeMonitor.class);

    private final Map<String, RoundTripTimeProgress> progressMap = new HashMap<>();
    private int finishedItems; // feeds or queries
    private int totalItems;    // feeds or queries

    protected boolean finished = false;

    public boolean isFinished() {
        return finished;
    }

    public void initWithProperties(ImmutableList<FeedProperty> properties) {
        this.reset();
        totalItems = properties.size();

        for (FeedProperty fp : properties) {
            final RoundTripTimeProgress rttp = new IndexRoundTripTimeProgress(fp.getUri(), fp.getNumberOfEpisodes());
            progressMap.put(fp.getUri(), rttp);
        }
    }

    public void initWithQueries(ImmutableList<String> queries) {
        this.reset();
        totalItems = queries.size();

        for (String q : queries) {
            final RoundTripTimeProgress rttp = new QueryRoundTripTimeProgress(q);
            progressMap.put(q, rttp);
        }
    }

    public void addRoundTripTime(RoundTripTime rtt) {

        if (rtt.getWorkflow() == null) {
            log.warn("Unable to process RTT -- workflow is NULL : {}", rtt);
            return;
        }

        if (!progressMap.containsKey(rtt.getId())) {
            log.warn("Unable to process RTT -- No progress registered for ID : {}", rtt.getId());
            return;
        }

        final RoundTripTimeProgress progress = progressMap.get(rtt.getId());
        final IndexRoundTripTimeProgress indexRTTP;
        final QueryRoundTripTimeProgress queryRTTP;
        switch (rtt.getWorkflow()) {
            case PODCAST_INDEX:
                indexRTTP = (IndexRoundTripTimeProgress) progress;
                indexRTTP.setPodcastRTT(rtt);
                break;
            case EPISODE_INDEX:
                indexRTTP = (IndexRoundTripTimeProgress) progress;
                indexRTTP.addEpisodeRTT(rtt);
                break;
            case RESULT_RETRIEVAL:
                queryRTTP = (QueryRoundTripTimeProgress) progress;
                queryRTTP.setResultsRTT(rtt);
                break;
            default:
                log.warn("Unknown Workflow : {} for RTT with URI : {}", rtt.getWorkflow(), rtt.getId());
                break;
        }

        if (progress.finished()) {
            finishedItems += 1;
            progress.calculateEvaluation();
        }

        if (finishedItems == totalItems) {
            finished = true;
        }
    }

    public void logResults() {

        if (!isFinished()) {
            log.info("Not all podcasts and episodes have reported completeness");
        }

        log.info("All expected podcasts and episodes have reported registration complete");

        long sumOverallRTT = 0;
        long sumMeanRTTPerEp = 0;
        for (RoundTripTimeProgress p : progressMap.values()) {
            log.info("[RTT] overall: {}ms\tmeanPerEp: {}ms\tmeanMsgL: {}ms\tid: {}", String.format("%1$6s", p.getOverallRoundTripTime()), String.format("%1$6s", p.getMeanRoundTripTime()), String.format("%1$8s", String.format("%7.1f", p.getMeanMessageLatency())), p.getId());

            sumOverallRTT += p.getOverallRoundTripTime();
            sumMeanRTTPerEp += p.getMeanRoundTripTime();
        }

        final double nrOfElements = (double) progressMap.values().size();
        final double meanOverallRTT = ((double) sumOverallRTT) / nrOfElements;
        final double overallMeanRTTPerEp = ((double) sumMeanRTTPerEp) / nrOfElements;
        printMetric("Mean overall RTT", String.format("%1$8s", String.format("%6.1f", meanOverallRTT)) + "ms");
        printMetric("Overall mean RTT", String.format("%1$8s", String.format("%6.1f", overallMeanRTTPerEp)) + "ms");
    }

    private void printMetric(String name, double metric) {
        log.info("[RTT] {} : {}ms", name, String.format("%1$6s", metric));
    }

    private void printMetric(String name, String value) {
        log.info("[RTT] {} : {}", name, value);
    }

    private void reset() {
        finished = false;
        progressMap.clear();
        finishedItems = 0;
    }

}
