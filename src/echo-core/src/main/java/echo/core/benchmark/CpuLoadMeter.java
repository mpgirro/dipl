package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class CpuLoadMeter extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CpuLoadMeter.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private final int interval;
    private final List<Double> dataPoints = new LinkedList<>();

    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    public CpuLoadMeter(int interval) {
        this.interval = interval;
        this.start();
    }

    public void startMonitoring() {
        log.debug("Starting the monitoring");
        monitoring.set(true);
        synchronized (dataPoints) {
            dataPoints.clear();
        }
    }

    public void stopMonitoring() {
        log.debug("Stopping the monitoring");
        monitoring.set(false);
    }

    public void halt() {
        log.debug("Halting the meter");
        running.set(false);
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                if (monitoring.get()) {
                    synchronized (dataPoints) {
                        dataPoints.add(getProcessCpuLoad());
                    }
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("Error on collecting datapoints : {}", e);
                e.printStackTrace();
            }
        }
    }

    public synchronized List<Double> getDataPoints() {
        synchronized (dataPoints) {
            return dataPoints;
        }
    }

    public synchronized double getProcessCpuLoad() {
        final Object value = invokeOperatingSystemMXBeanMethod("getProcessCpuLoad");
        return (value == null) ? 0.0 : (double) value;
    }

    public synchronized double getMeanCpuLoad() {
        double result = 0;
        synchronized (dataPoints) {
            if (dataPoints.size() > 0) {
                final double sum = dataPoints.stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

                result = sum / dataPoints.size();
            }
        }
        return result;
    }

    private synchronized Object invokeOperatingSystemMXBeanMethod(String methodName) {
        Object value = null;
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().equals(methodName) && Modifier.isPublic(method.getModifiers())) {
                try {
                    value = method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                    value = e;
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
