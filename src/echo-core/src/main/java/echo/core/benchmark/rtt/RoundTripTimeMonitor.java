package echo.core.benchmark.rtt;

import com.google.common.collect.ImmutableList;
import echo.core.benchmark.ArchitectureType;
import echo.core.benchmark.FeedProperty;
import echo.core.benchmark.Workflow;
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

    private final ArchitectureType architectureType;
    private final Map<String, RoundTripTimeProgress> progressMap = new HashMap<>();
    private int finishedItems; // feeds or queries
    private int totalItems;    // feeds or queries

    private Workflow workflow;

    private long overallRuntime;
    private double meanRttPerFeed;
    private double meanRttPerItem;

    protected boolean finished = false;

    public RoundTripTimeMonitor(ArchitectureType architectureType) {
        this.architectureType = architectureType;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized void initWithProperties(ImmutableList<FeedProperty> properties) {
        this.reset();
        workflow = Workflow.PODCAST_INDEX;
        totalItems = properties.size();

        for (FeedProperty fp : properties) {
            final RoundTripTimeProgress rttp = new IndexRoundTripTimeProgress(fp.getUri(), fp.getNumberOfEpisodes());
            progressMap.put(fp.getUri(), rttp);
        }
    }

    public synchronized void initWithQueries(ImmutableList<String> queries) {
        this.reset();
        workflow = Workflow.RESULT_RETRIEVAL;
        totalItems = queries.size();

        for (String q : queries) {
            final RoundTripTimeProgress rttp = new QueryRoundTripTimeProgress(q);
            progressMap.put(q, rttp);
        }
    }

    public synchronized void addRoundTripTime(RoundTripTime rtt) {

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
            calculateMetrics();
        }
    }

    public synchronized void logResults() {

        ensureFinished();

        log.info("All expected podcasts and episodes have reported registration complete");

        for (RoundTripTimeProgress p : progressMap.values()) {
            log.info("[RTT] overall: {}ms\tmeanPerEp: {}ms\tmeanMsgL: {}ms\tid: {}", formatLong(p.getOverallRoundTripTime()), formatDouble(p.getMeanRoundTripTime()), formatDouble(p.getMeanMessageLatency()), p.getId());
        }

        printSumEvals();
    }

    public synchronized String getProgressCSV() {

        ensureFinished();

        final StringBuilder builder = new StringBuilder();
        builder.append("src;overallRT;meanRTpI;meanMsgL;uri\n");
        for (Map.Entry<String,RoundTripTimeProgress> e : progressMap.entrySet()) {
            final RoundTripTimeProgress p = e.getValue();
            builder.append(architectureType +";"+ formatLong(p.getOverallRoundTripTime())+";"+formatDouble(p.getMeanRoundTripTime())+";"+ formatDouble(p.getMeanMessageLatency())+";"+e.getKey()+"\n");
        }
        return builder.toString();
    }

    public synchronized String getOverallCSV() {

        ensureFinished();

        return //"src;input_size;overallRT;mean_rtt_per_feed;mean_rtt_per_item\n" +
            architectureType +
            ";" +
            progressMap.size() +
            ";" +
            overallRuntime +
            ";" +
            meanRttPerFeed +
            ";" +
            meanRttPerItem +
            "\n";
    }

    public synchronized List<RoundTripTime> getAllRTTs() {
        final ImmutableList.Builder<RoundTripTime> builder = ImmutableList.builder();
        for (RoundTripTimeProgress p : progressMap.values()) {
            builder.addAll(p.getAllRTTs());
        }
        return builder.build();
    }

    private synchronized void calculateMetrics() {

        ensureFinished();

        long sumMeanRttPerFeed = 0;
        long sumMeanRttPerItem = 0;
        long earliestTimestamp = Long.MAX_VALUE;
        long latestTimestamp = 0;
        for (RoundTripTimeProgress p : progressMap.values()) {
            sumMeanRttPerFeed += p.getOverallRoundTripTime();
            sumMeanRttPerItem += p.getMeanRoundTripTime();

            if (p.firstTimestamp < earliestTimestamp) {
                earliestTimestamp = p.firstTimestamp;
            }

            if (latestTimestamp < p.lastTimestamp) {
                latestTimestamp = p.lastTimestamp;
            }
        }

        final double nrOfElements = (double) progressMap.values().size();
        this.overallRuntime = latestTimestamp - earliestTimestamp;
        this.meanRttPerFeed = ((double) sumMeanRttPerFeed) / nrOfElements;
        this.meanRttPerItem = ((double) sumMeanRttPerItem) / nrOfElements;
    }

    public synchronized long getOverallRuntime() {
        return this.overallRuntime;
    }

    public synchronized double getMeanRttPerFeed() {
        return this.meanRttPerFeed;
    }

    public synchronized double getMeanRttPerItem() {
        return this.meanRttPerItem;
    }

    public synchronized Workflow getWorkflow() {
        return workflow;
    }

    private void printMetric(String name, double metric) {
        log.info("[RTT] {} : {}ms", name, String.format("%1$7s", metric));
    }

    public synchronized void printSumEvals() {

        ensureFinished();

        printMetric("Mean overall RTT per Feed  ", String.format("%1$8s", String.format("%6.1f", meanRttPerFeed)) + "ms");
        printMetric("Overall mean RTT per Item  ", String.format("%1$8s", String.format("%6.1f", meanRttPerItem)) + "ms");
        printMetric("Overall RT w.r.t Input Size", overallRuntime + "ms");
    }

    private synchronized void printMetric(String name, String value) {
        log.info("[RTT] {} : {}", name, value);
    }

    private synchronized String formatLong(long value) {
        return String.format("%1$6s", value);
    }

    private synchronized String formatDouble(double value) {
        return String.format("%1$8s", String.format("%7.1f", value));
    }

    private synchronized void reset() {
        finished = false;
        progressMap.clear();
        finishedItems = 0;
        workflow = null;
    }

    private synchronized void ensureFinished() {
        if (!isFinished()) {
            throw new RuntimeException("Invalid access of result value; RTT monitoring is not yet finished");
        }
    }

}
