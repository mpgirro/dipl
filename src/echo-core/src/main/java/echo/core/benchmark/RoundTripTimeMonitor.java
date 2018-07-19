package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Maximilian Irro
 */
public class RoundTripTimeMonitor {

    private static final Logger log = LoggerFactory.getLogger(RoundTripTimeMonitor.class);

    private final Map<String, RoundTripTimeProgress> progressMap = new HashMap<>();
    private int finishedFeeds;
    private int totalFeeds;

    private boolean finished = false;

    public boolean isFinished() {
        return finished;
    }

    public void initWithProperties(List<FeedProperty> properties) {

        progressMap.clear();
        finishedFeeds = 0;
        totalFeeds = properties.size();

        for (FeedProperty fp : properties) {
            final RoundTripTimeProgress rttp = new RoundTripTimeProgress(fp.getUri(), fp.getNumberOfEpisodes());
            progressMap.put(fp.getUri(), rttp);
        }
    }

    public void addRoundTripTime(RoundTripTime rtt) {

        if (rtt.getWorkflow() == null) {
            log.warn("Unable to process RTT -- workflow is NULL : {}", rtt);
            return;
        }

        if (!progressMap.containsKey(rtt.getUri())) {
            log.warn("Unable to process RTT -- No progress registered for URI : {}", rtt.getUri());
            return;
        }

        final RoundTripTimeProgress progress = progressMap.get(rtt.getUri());
        switch (rtt.getWorkflow()) {
            case PODCAST_INDEX:
                progress.setPodcastRTT(rtt);
                break;
            case EPISODE_INDEX:
                progress.addEpisodeRTT(rtt);
                break;
            case RESULT_RETRIEVAL:

                // TODO

                break;
            default:
                log.warn("Unknown Workflow : {} for RTT with URI : {}", rtt.getWorkflow(), rtt.getUri());
                break;
        }

        if (progress.finished()) {
            finishedFeeds += 1;
        }

        if (finishedFeeds == totalFeeds) {
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
            p.calculateEvaluation();
            log.info("[RTT] overall: {}ms\tmeanPerEp: {}ms\turl: {}", String.format("%1$6s", p.getOverallRoundTripTime()), String.format("%1$6s", p.getMeanRoundTripTimePerEpisode()), p.getUri());

            sumOverallRTT += p.getOverallRoundTripTime();
            sumMeanRTTPerEp += p.getMeanRoundTripTimePerEpisode();
        }

        final double nrOfElements = (double) progressMap.values().size();
        final double meanOverallRTT = ((double) sumOverallRTT) / nrOfElements;
        final double overallMeanRTTPerEp = ((double) sumMeanRTTPerEp) / nrOfElements;
        printMetric("Mean overall RTT     ", String.format("%1$8s", String.format("%6.1f", meanOverallRTT)));
        printMetric("Overall mean RTT / Ep", String.format("%1$8s", String.format("%6.1f", overallMeanRTTPerEp)));
    }

    private void printMetric(String name, double metric) {
        log.info("[RTT] {} : {}ms", name, String.format("%1$6s", metric));
    }

    private void printMetric(String name, String value) {
        log.info("[RTT] {} : {}", name, value);
    }

}
