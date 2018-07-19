package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Maximilian Irro
 */
public class MessagesPerSecondMonitor {

    private static final Logger log = LoggerFactory.getLogger(MessagesPerSecondMonitor.class);

    final Map<String, Double> map = new HashMap<>();

    public void addMetric(String name, double mps) {
        map.put(name, mps);
    }

    public void addAndPrintMetric(String name, double mps) {
        addMetric(name, mps);
        printMetric(name, mps);
    }

    private void printMetric(String name, double metric) {
        log.info("[MPS] {}mps   {}", String.format("%1$5s", String.format("%3.1f", metric)), name);
    }

}
