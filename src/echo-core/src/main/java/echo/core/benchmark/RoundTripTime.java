package echo.core.benchmark;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

import javax.annotation.Nullable;

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
@JsonSerialize(as = ImmutableRoundTripTime.class)
@JsonDeserialize(as = ImmutableRoundTripTime.class)
public interface RoundTripTime {

    String getUri();

    String getLocation();

    @Nullable
    Workflow getWorkflow();

    ImmutableList<Long> getRtts();

    default RoundTripTime bumpRTTs() {
        return ImmutableRoundTripTime.builder()
            .from(this)
            .addRtts(System.currentTimeMillis())
            .create();
    }

    static RoundTripTime empty() {
        return ImmutableRoundTripTime.builder()
            .setUri("")
            .setLocation("")
            .create();
    }

}
