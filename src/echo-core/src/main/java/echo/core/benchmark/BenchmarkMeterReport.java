package echo.core.benchmark;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * @author Maximilian Irro
 */
@Value.Immutable
@Value.Style(
    jdkOnly    = true,              // prevent usage of Guava collections
    get        = {"is*", "get*"},   // Detect 'get' and 'is' prefixes in accessor methods
    init       = "set*",
    create     = "new",             // generates public no args constructor
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableBenchmarkMeterReport.class)
@JsonDeserialize(as = ImmutableBenchmarkMeterReport.class)
public interface BenchmarkMeterReport {

    @Value.Parameter
    String getName();

    @Value.Parameter
    MessagesPerSecondResult getMps();

    @Value.Parameter
    MemoryUsageResult getMemoryUsage();

    @Value.Parameter
    CpuLoadResult getCpuLoad();

}
