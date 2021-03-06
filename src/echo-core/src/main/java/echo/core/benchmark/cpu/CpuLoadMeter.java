package echo.core.benchmark.cpu;

import echo.core.benchmark.BenchmarkMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class CpuLoadMeter extends Thread implements BenchmarkMeter {

    private static final Logger log = LoggerFactory.getLogger(CpuLoadMeter.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean measuring = new AtomicBoolean(false);
    private final List<Double> dataPoints = new LinkedList<>();
    private final String name;
    private final int interval;

    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    private CpuLoadResult result;

    public CpuLoadMeter(String name, int interval) {
        this.name = name;
        this.interval = interval;
        this.start();
    }

    @Override
    public synchronized void activate() {
        log.debug("{} : Activating the CpuLoadMeter", name);
        running.set(true);
        this.start();
    }

    @Override
    public void deactivate() {
        log.debug("{} : Deactivating the CpuLoadMeter", name);
        running.set(true);
    }

    @Override
    public void startMeasurement() {
        log.debug("{} : Starting the CPU load measurement", name);
        if (!isMeasuring()) {
            synchronized (dataPoints) {
                dataPoints.clear();
            }
        }
        measuring.set(true);
    }

    @Override
    public void stopMeasurement() {
        log.debug("{} : Stopping the CPU load measurement", name);
        if (isMeasuring()) {
            synchronized (dataPoints) {
                result = CpuLoadResult.of(name, dataPoints);
            }
        }
        measuring.set(false);
    }

    @Override
    public boolean isActive() {
        return running.get();
    }

    @Override
    public boolean isMeasuring() {
        return measuring.get();
    }

    @Override
    public void run() {
        running.set(true);
        while (isActive()) {
            try {
                if (isActive()) {
                    synchronized (dataPoints) {
                        dataPoints.add(getProcessCpuLoad());
                    }
                }
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.error("{} : Error on collecting datapoints : {}", name, e);
                e.printStackTrace();
            }
        }
    }

    public synchronized CpuLoadResult getResult() {
        return Optional
            .ofNullable(result)
            .orElseThrow(() -> new RuntimeException(name + " : CPU load result not yet available"));
    }

    private synchronized double getProcessCpuLoad() {
        final Object value = invokeOperatingSystemMXBeanMethod("getProcessCpuLoad");
        return (value == null) ? 0.0 : (double) value;
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
