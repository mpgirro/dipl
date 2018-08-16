package echo.core.benchmark.rtt;

import com.google.common.collect.ImmutableList;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Irro
 */
public class IndexRoundTripTimeProgress extends RoundTripTimeProgress {

    private static final Logger log = LoggerFactory.getLogger(IndexRoundTripTimeProgress.class);

    private int totalEpisodes;

    private RoundTripTime podcastRTT;
    private final List<RoundTripTime> episodeRTTs = new LinkedList<>();

    public IndexRoundTripTimeProgress(String id, int totalEpisodes) {
        super(id);
        this.totalEpisodes = totalEpisodes;
    }

    @Override
    public boolean finished() {
        return (podcastRTT!=null) && (episodeRTTs.size() == totalEpisodes);
    }

    public RoundTripTime getPodcastRTT() {
        return podcastRTT;
    }

    public void setPodcastRTT(RoundTripTime podcastRTT) {
        this.podcastRTT = podcastRTT;
    }

    public List<RoundTripTime> getEpisodeRTTs() {
        return episodeRTTs;
    }

    public void addEpisodeRTT(RoundTripTime rtt) {
        this.episodeRTTs.add(rtt);
    }

    @Override
    public void calculateEvaluation() {

        if (!finished()) {
            throw new RuntimeException("Cannot calculate IndexRoundTripTimeProgress evaluation -- not yet finished");
        }

        firstTimestamp = podcastRTT.getRtts().get(0);
        lastTimestamp = podcastRTT.getRtts().get(podcastRTT.getRtts().size()-1);

        long overallRoundTripTime = podcastRTT.getLastTimestamp() - podcastRTT.getFirstTimestamp();
        double overallMessageLatency = podcastRTT.getMeanMessageLatency();

        for (RoundTripTime rtt : episodeRTTs) {
            if (rtt.getFirstTimestamp() < firstTimestamp) {
                firstTimestamp = rtt.getRtts().get(0);
            }

            if (rtt.getLastTimestamp() > lastTimestamp) {
                lastTimestamp = rtt.getLastTimestamp();
            }

            overallRoundTripTime += rtt.getLastTimestamp() - rtt.getFirstTimestamp();
            overallMessageLatency += rtt.getMeanMessageLatency();
        }

        meanRoundTripTime  = overallRoundTripTime  / (1 + totalEpisodes);  // integer devision rounding is fine, we have milliseconds anyway
        meanMessageLatency = overallMessageLatency / (1 + totalEpisodes);  // mean of podcast and all episodes
    }

    @Override
    public List<RoundTripTime> getAllRTTs() {
        final ImmutableList.Builder<RoundTripTime> builder = ImmutableList.builder();
        if (podcastRTT != null) {
            builder.add(podcastRTT);
        }
        builder.addAll(episodeRTTs);
        return builder.build();
    }

}
