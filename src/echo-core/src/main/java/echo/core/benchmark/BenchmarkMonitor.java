package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Irro
 */
public class BenchmarkMonitor {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkMonitor.class);

    private final ArchitectureType architectureType;

    private final List<BenchmarkMeterReport> reports = new LinkedList<>();
    private long expectedReports;

    public BenchmarkMonitor(ArchitectureType architectureType) {
        this.architectureType = architectureType;
    }

    public synchronized void init(long expectedReports) {
        this.expectedReports = expectedReports;
        synchronized (reports) {
            reports.clear();
        }
    }

    public synchronized void addReport(BenchmarkMeterReport report) {
        synchronized (reports) {
            reports.add(report);
        }
    }

    public synchronized boolean isFinished() {
        synchronized (reports) {
            return reports.size() == expectedReports;
        }
    }

    public synchronized String toCsv() {
        synchronized (reports) {
            ensureFinished();

            final StringBuilder builder = new StringBuilder();
            builder.append("src;mps;cpu;mem\n");
            for (BenchmarkMeterReport r : reports) {
                builder
                    .append(r.getName())
                    .append(";")
                    .append(r.getMps().getMpsAsString())
                    .append(";")
                    .append(r.getCpuLoad().getMeanLoadAsString())
                    .append(";")
                    .append(r.getMemoryUsage().getMeanBytesAsString())
                    .append("\n");
            }
            return builder.toString();
        }
    }

    private void ensureFinished() {
        if (!isFinished()) {
            throw new RuntimeException("Invalid access of result value; RTT monitoring is not yet finished");
        }
    }

}
