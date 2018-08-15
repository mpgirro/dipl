package echo.core.benchmark.mps;

import com.google.common.collect.ImmutableList;

import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondResult {

    public final ImmutableList<Long> dataPoints;
    public final double mps;
    public final String mpsStr;

    private MessagesPerSecondResult(Map<Long, Long> buckets) {
        long messagesCount = 0;
        long secondsCount  = 0;

        for (Long b : buckets.keySet()) {
            secondsCount += 1;
            messagesCount += buckets.get(b);
        }

        dataPoints = buckets.keySet()
            .stream()
            .sorted()
            .map(buckets::get)
            .collect(collectingAndThen(toList(), ImmutableList::copyOf));
        final double m = (double) messagesCount;
        final double s = (double) secondsCount;
        mps = (s > 0) ? (m / s) : 0.0;
        mpsStr = "" + ((double) Math.round(mps * 100) / 100);
    }

    public static MessagesPerSecondResult of(Map<Long, Long> buckets) {
        return new MessagesPerSecondResult(buckets);
    }

}
