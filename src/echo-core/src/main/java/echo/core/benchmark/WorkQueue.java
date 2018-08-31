package echo.core.benchmark;

import echo.core.benchmark.mps.MessagesPerSecondMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class WorkQueue {

    private static final Logger log = LoggerFactory.getLogger(WorkQueue.class);

    private final String queueName;
    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private final MessagesPerSecondMeter mpsMeter;

    public WorkQueue(String name, int nThreads) {
        this.queueName = name;
        this.nThreads = nThreads;
        queue = new LinkedList<>();
        threads = new PoolWorker[nThreads];
        mpsMeter = new MessagesPerSecondMeter(name);

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {

        if (!mpsMeter.isMeasuring()) {
            mpsMeter.startMeasurement();
        }

        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public void executeAll(Collection<Runnable> rs) {

        if (!mpsMeter.isMeasuring()) {
            mpsMeter.startMeasurement();
        }

        synchronized(queue) {
            queue.addAll(rs);
            queue.notify();
        }
    }

    public boolean isFinished() {
        synchronized(queue) {
            return queue.isEmpty();
        }
    }

    public void shutdown() {
        log.debug("Shutting down the work queue");
        synchronized(queue) {
            for (PoolWorker w : threads) {
                w.halt();
            }
            queue.clear();
        }
        log.debug("All workers halted and queue cleared");
    }

    private class PoolWorker extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(false);

        public void halt() {
            running.set(false);
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                Runnable r;
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        }
                        catch (InterruptedException ignored) {
                        }
                    }

                    r = queue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();

                    mpsMeter.tick();
                    if (isFinished()) {
                        mpsMeter.stopMeasurement();
                        log.info("{} was able to execute {} Tasks Per Second", queueName, mpsMeter.getResult().getMpsAsString());
                    }
                }
                catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }

    }
}
