package echo.core.benchmark.cpu;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public class CpuLoadResult {

    public final String name;
    public final ImmutableList<Double> dataPoints;
    public final double meanLoad;
    public final String meanLoadStr;

    private CpuLoadResult(String name, List<Double> dataPoints) {
        this.name = name;
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

    public static CpuLoadResult of(String name, List<Double> dataPoints) {
        return new CpuLoadResult(name, dataPoints);
    }

    @Override
    public String toString() {
        return "CpuLoadResult{" +
            "name='" + name + '\'' +
            ", meanLoad=" + meanLoad +
            ", meanLoadStr='" + meanLoadStr + '\'' +
            ", dataPoints=" + dataPoints +
            '}';
    }
}
