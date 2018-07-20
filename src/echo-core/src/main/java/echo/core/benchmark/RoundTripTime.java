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

    String getId();

    String getLocation();

    @Nullable
    Workflow getWorkflow();

    ImmutableList<Long> getRtts();

    static RoundTripTime empty() {
        return ImmutableRoundTripTime.builder()
            .setId("")
            .setLocation("")
            .create();
    }

    default RoundTripTime bumpRTTs() {
        return ImmutableRoundTripTime.builder()
            .from(this)
            .addRtts(System.currentTimeMillis())
            .create();
    }

    default long overallRuntime() {
        final ImmutableList<Long> rtts = getRtts();
        if (rtts.isEmpty()) {
            return 0;
        } else {
            return rtts.get(rtts.size()-1) - rtts.get(0);
        }
    }

    default long getFirstTimestamp() {
        return (getRtts().size() > 0) ? getRtts().get(0) : 0;
    }

    default long getLastTimestamp() {
        return (getRtts().size() > 0) ? getRtts().get(getRtts().size()-1) : 0;
    }

    default double getMeanMessageLatency() {
        final ImmutableList<Long> rtts = getRtts();

        final int nrOfElems = rtts.size();
        if (nrOfElems < 2) return 0;

        long sumLatency = 0;
        for (int i = 0; i < nrOfElems-2; i++) {
            final long latency = rtts.get(i+1) - rtts.get(i);
            sumLatency += latency;
        }
        return ((double) sumLatency) / ((double) nrOfElems);
    }

}
