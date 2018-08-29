package echo.core.benchmark.mps;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

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
@JsonSerialize(as = ImmutableMessagesPerSecondResult.class)
@JsonDeserialize(as = ImmutableMessagesPerSecondResult.class)
public interface MessagesPerSecondResult {

    @Value.Parameter
    String getName();

    @Value.Parameter
    Double getMps();

    @Value.Parameter
    List<Long> dataPoints();

    static MessagesPerSecondResult of(String name, Map<Long, Long> buckets) {

        long messagesCount = 0;
        long secondsCount  = 0;
        for (Long b : buckets.keySet()) {
            secondsCount += 1;
            messagesCount += buckets.get(b);
        }

        final double m = (double) messagesCount;
        final double s = (double) secondsCount;
        final double mps = (s > 0) ? (m / s) : 0.0;
        final String mpsStr = "" + ((double) Math.round(mps * 100) / 100);

        final ImmutableList<Long> dataPoints = buckets.keySet()
            .stream()
            .sorted()
            .map(buckets::get)
            .collect(collectingAndThen(toList(), ImmutableList::copyOf));

        return ImmutableMessagesPerSecondResult.of(name, mps, dataPoints);
    }

    default String getMpsAsString() {
        return String.format("%.4f", getMps());
    }

}
