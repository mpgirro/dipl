package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Irro
 */
public class RoundTripTimeProgress {

    private static final Logger log = LoggerFactory.getLogger(RoundTripTimeProgress.class);

    private final String uri;
    private int totalEpisodes;

    private long firstTimestamp;
    private long lastTimestamp;
    private long meanRoundTripTimePerEpisode;

    private RoundTripTime podcastRTT;
    private List<RoundTripTime> episodeRTTs = new LinkedList<>();

    public RoundTripTimeProgress(String uri, int totalEpisodes) {
        this.uri = uri;
        this.totalEpisodes = totalEpisodes;
    }

    public String getUri() {
        return uri;
    }

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

    public void calculateEvaluation() {
        firstTimestamp = podcastRTT.getRtts().get(0);
        lastTimestamp = podcastRTT.getRtts().get(podcastRTT.getRtts().size()-1);

        long overallEpisodeTime = 0;

        for (RoundTripTime rtt : episodeRTTs) {
            if (rtt.getFirstTimestamp() < firstTimestamp) {
                firstTimestamp = rtt.getRtts().get(0);
            }

            if (rtt.getLastTimestamp() > lastTimestamp) {
                lastTimestamp = rtt.getLastTimestamp();
            }

            overallEpisodeTime += rtt.getLastTimestamp() - rtt.getFirstTimestamp();
        }

        meanRoundTripTimePerEpisode = overallEpisodeTime / totalEpisodes; // integer devision rounding is fine, we have milliseconds anyway

    }

    public long getOverallRoundTripTime() {
        return lastTimestamp - firstTimestamp;
    }

    public long getMeanRoundTripTimePerEpisode() {
        return meanRoundTripTimePerEpisode;
    }

}
