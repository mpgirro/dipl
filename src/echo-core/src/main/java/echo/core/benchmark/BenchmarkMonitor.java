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

    public void init(long expectedReports) {
        this.expectedReports = expectedReports;
        reports.clear();
    }

    public void addReport(BenchmarkMeterReport report) {
        reports.add(report);
    }

    public boolean isFinished() {
        return reports.size() == expectedReports;
    }

    public String toCsv() {
        ensureFinished();

        final StringBuilder builder = new StringBuilder();
        builder.append("architecture;mps;cpu;msm\n");
        for (BenchmarkMeterReport r : reports) {
            builder
                .append(architectureType)
                .append(";")
                .append(r.getMps().getMps())
                .append(";")
                .append(r.getCpuLoad().getMeanLoad())
                .append(";")
                .append(r.getMemoryUsage().getMeanBytes())
                .append("\n");
        }
        return builder.toString();
    }

    private void ensureFinished() {
        if (!isFinished()) {
            throw new RuntimeException("Invalid access of result value; RTT monitoring is not yet finished");
        }
    }

}
