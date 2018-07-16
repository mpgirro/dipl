package echo.core.benchmark;

import com.google.common.collect.ImmutableList;

/**
 * @author Maximilian Irro
 */
public class RoundTripTime {

    public static synchronized ImmutableList<Long> appendNow(ImmutableList<Long> rtts) {
        return ImmutableList.<Long>builder()
            .addAll(rtts)
            .add(System.currentTimeMillis())
            .build();
    }

}
