package echo.core.benchmark.memory;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public class MemoryUsageResult {

    private static final long MEGABYTE = 1000L * 1000L; // base 10, not base 2

    public final ImmutableList<Long> dataPoints;
    public final double meanBytes;
    public final String meanBytesStr;

    private MemoryUsageResult(List<Long> dataPoints) {
        this.dataPoints = ImmutableList.copyOf(dataPoints);

        double tmp = 0;
        if (dataPoints.size() > 0) {
            final long sum = dataPoints.stream()
                .mapToLong(Long::longValue)
                .sum();

            tmp = ((double) sum) / dataPoints.size();
        }
        meanBytes = tmp;
        meanBytesStr = (((long) meanBytes) / MEGABYTE) + " MB";
    }

    public static MemoryUsageResult of(List<Long> dataPoints) {
        return new MemoryUsageResult(dataPoints);
    }

    /* TODO
    public double getMeanBytes() {
        return meanBytes;
    }

    public String getMeanBytesAsString() {
        return meanBytesStr;
    }

    public List<Long> getDataPoints() {
        return dataPoints;
    }
    */

}
