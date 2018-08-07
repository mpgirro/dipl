package echo.core.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Maximilian Irro
 */
public class WorkQueue {

    private static final Logger log = LoggerFactory.getLogger(WorkQueue.class);

    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public WorkQueue(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedList<>();
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    public void executeAll(Collection<Runnable> rs) {
        synchronized(queue) {
            queue.addAll(rs);
            queue.notify();
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
                }
                catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }
}
