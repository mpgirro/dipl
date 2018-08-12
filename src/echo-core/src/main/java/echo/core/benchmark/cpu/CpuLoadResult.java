package echo.core.benchmark.cpu;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public class CpuLoadResult {

    public final ImmutableList<Double> dataPoints;
    public final double meanLoad;
    public final String meanLoadStr;

    private CpuLoadResult(List<Double> dataPoints) {
        this.dataPoints = ImmutableList.copyOf(dataPoints);

        double tmp = 0;
        if (dataPoints.size() > 0) {
            final double sum = dataPoints.stream()
                .mapToDouble(Double::doubleValue)
                .sum();

            tmp = sum / dataPoints.size();
        }
        meanLoad = tmp;
        meanLoadStr = "" + ((double) Math.round(meanLoad * 100) / 100);
    }

    public static CpuLoadResult of(List<Double> dataPoints) {
        return new CpuLoadResult(dataPoints);
    }

    /* TODO
    public double getMeanLoad() {
        return meanLoad;
    }
    public String getMeanLoadAsString() {
        return meanLoadStr;
    }

    public List<Double> getDataPoints() {
        return dataPoints;
    }
    */

}
