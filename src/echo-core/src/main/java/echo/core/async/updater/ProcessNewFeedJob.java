package echo.core.async.updater;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import echo.core.benchmark.rtt.RoundTripTime;
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
    defaults   = @Value.Immutable(builder = false),  // We may also disable builder
    build      = "create"           // rename 'build' method on builder to 'create'
)
@JsonSerialize(as = ImmutableProcessNewFeedJob.class)
@JsonDeserialize(as = ImmutableProcessNewFeedJob.class)
public interface ProcessNewFeedJob extends UpdaterJob {

    @Value.Parameter
    String getExo();

    @Value.Parameter
    String getFeed();

    @Value.Parameter
    RoundTripTime getRtt();
}
