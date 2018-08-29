package echo.core.benchmark.mps;

import com.google.common.collect.Lists;
import echo.core.benchmark.ArchitectureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<Double> getDataPoints() {
        return Lists.newLinkedList(mpsMap.values());
    }

    public double getMeanMps() {

        if (!isFinished()) {
            throw new RuntimeException("Not all tasks have reported metrics yet");
        }

        if (mpsMap.size() <= 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (Map.Entry<String,Double> e : mpsMap.entrySet()) {
            sum += e.getValue();
        }

        return sum / ((double) mpsMap.size());

    }

    public String toCsv() {

        if (!isFinished()) {
            throw new RuntimeException("Not all tasks have reported metrics yet");
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("src;mps;task_id\n");

        final List<Map.Entry<String,Double>> entries = mpsMap.entrySet()
            .stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .collect(Collectors.toList());

        for (Map.Entry<String,Double> e : entries) {
            builder
                .append(type)
                .append(";")
                .append(metricToString(e.getValue()))
                .append(";")
                .append(e.getKey())
                .append("\n");
        }
        return builder.toString();
    }

    private String metricToString(double metric) {
        return String.format("%1$7s", String.format("%3.3f", metric));
    }

}
