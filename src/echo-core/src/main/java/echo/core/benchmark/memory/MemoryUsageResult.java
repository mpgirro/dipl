package echo.core.benchmark.memory;

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
@JsonSerialize(as = ImmutableMemoryUsageResult.class)
@JsonDeserialize(as = ImmutableMemoryUsageResult.class)
public interface MemoryUsageResult {

    long MEGABYTE = 1000L * 1000L; // base 10, not base 2

    @Value.Parameter
    String getName();

    @Value.Parameter
    Double getMeanBytes();

    @Value.Parameter
    List<Long> dataPoints();

    static MemoryUsageResult of(String name, List<Long> dataPoints) {
        double meanBytes = 0;
        if (dataPoints.size() > 0) {
            final long sum = dataPoints.stream()
                .mapToLong(Long::longValue)
                .sum();
            if (sum > 0) {
                meanBytes = ((double) sum) / dataPoints.size();
            }
        }

        return ImmutableMemoryUsageResult.of(name, meanBytes, dataPoints);
    }

    default String getMeanBytesAsString() {
        return Long.toString(((long) (double) getMeanBytes()) / MEGABYTE);
    }

    default String getMeanBytesAsStringWithUnit() {
        return getMeanBytesAsString() + " MB";
    }
}
