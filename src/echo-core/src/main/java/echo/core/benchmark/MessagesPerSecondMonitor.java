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

    private final ArchitectureType type;
    private final int nrOfTasks;
    private final Map<String, Double> mpsMap = new HashMap<>();

    public MessagesPerSecondMonitor(ArchitectureType type, int nrOfTasks) {
        this.type = type;
        this.nrOfTasks = nrOfTasks;
    }

    public void reset() {
        mpsMap.clear();
    }

    public boolean isFinished() {
        return mpsMap.size() == nrOfTasks;
    }

    public void addMetric(String name, double mps) {
        mpsMap.put(name, mps);
    }

    public void addAndPrintMetric(String name, double mps) {
        addMetric(name, mps);
        printMetric(name, mps);
    }

    private void printMetric(String name, double metric) {
        log.info("[MPS] {}mps   {}", metricToString(metric), name);
    }

    public String toCsv() {

        if (!isFinished()) {
            log.info("Not all tasks have reported metrics yet");
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("src;mps;task-id\n");
        for (Map.Entry<String,Double> e : mpsMap.entrySet()) {
            builder.append(type+";"+metricToString(e.getValue())+";"+e.getKey()+"\n");
        }
        return builder.toString();
    }

    private String metricToString(double metric) {
        return String.format("%1$7s", String.format("%3.3f", metric));
    }

}
