package echo.core.benchmark.cpu;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Style(
    jdkOnly    = true,              // prevent usage of Guava collections
    get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",             // generates public no args constructor
    defaults   = @Value.Immutable(builder = false),  // We may also disable builder
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableCpuLoadResult.class)
@JsonDeserialize(as = ImmutableCpuLoadResult.class)
public interface CpuLoadResult {

    @Value.Parameter
    String getName();

    @Value.Parameter
    Double getMeanLoad();

    @Value.Parameter
    List<Double> dataPoints();

    static CpuLoadResult of(String name, List<Double> dataPoints) {
        double meanLoad = 0;
        if (dataPoints.size() > 0) {
            final double sum = dataPoints.stream()
                .mapToDouble(Double::doubleValue)
                .sum();

            meanLoad = sum / dataPoints.size();
        }

        return ImmutableCpuLoadResult.of(name, meanLoad, dataPoints);
    }

    default String getMeanLoadAsString() {
        return String.format("%.4f", getMeanLoad());
    }


}
