package echo.core.benchmark;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
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
@JsonSerialize(as = ImmutableBenchmark.class)
@JsonDeserialize(as = ImmutableBenchmark.class)
public interface Benchmark {

    String getUri();

    String getLocation();

    ImmutableList<Long> getRtts();

    default Benchmark bumpRTTs() {
        return ImmutableBenchmark.builder()
            .from(this)
            .addRtts(System.currentTimeMillis())
            .create();
    }

    static Benchmark empty() {
        return ImmutableBenchmark.builder()
            .setUri("")
            .setLocation("")
            .create();
    }

}
