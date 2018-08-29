package echo.core.benchmark;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.benchmark.rtt.RoundTripTime;
import org.immutables.value.Value;

import java.util.Optional;

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
@JsonSerialize(as = ImmutableBenchmarkSearchRequest.class)
@JsonDeserialize(as = ImmutableBenchmarkSearchRequest.class)
public interface BenchmarkSearchRequest {

    String getQuery();

    Optional<Integer> getPage();

    Optional<Integer> getSize();

    RoundTripTime getRtt();

}
