package echo.core.benchmark;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface BenchmarkMeter {

    void activate();

    void deactivate();

    void startMeasurement();

    void stopMeasurement();

}
