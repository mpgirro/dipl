package echo.core.benchmark;

/**
 * @author Maximilian Irro
 */
public interface BenchmarkMeter {

    void activate();

    void deactivate();

    void startMeasurement();

    void stopMeasurement();

    boolean isActive();

    boolean isMeasuring();

}
