package com.hpcjava81.crypton.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Simple thread-safe object pool.
 * <br>
 * <br>
 * An object is retrieved by a call to {@link #get()} and released back to the
 * pool via {@link #release(Object)}. The pool grows by a factor equal to
 * <code>growthFactor</code> but stops growing if the pool size breaches the
 * <code>thresholdFactor</code>, in which case, it becomes a simple object factory
 * with no pooling.
 * <br>
 * <br>
 * <pre><b>Usage</b>:
 * {@code
 *  T t;
 *  try {
 *      t = objPool.get()
 *      ...
 *  } finally {
 *      objPool.release(t)
 *  }
 * }
 *
 * </pre>
 *
 * <b>Note:</b> It is recommended (not mandatory) that the pooled objects implement
 * the <code>Reusable</code> interface to allow auto reset.
 *
 * @param <T> the type of objects to pool
 */
public class ReusableObjPool<T> {
    private static final Logger log = LoggerFactory.getLogger(ReusableObjPool.class);

    private ConcurrentLinkedQueue<T> queue;
    private Supplier<T> objSupplier;

    private int initialPoolSize;
    private int poolSize;
    private int growthFactor;
    private int thresholdFactor;

    private AtomicInteger capacityCheckCAS = new AtomicInteger(0);

    private volatile boolean possibleMemoryLeak = false;

    public ReusableObjPool(Supplier<T> objSupplier, int poolSize) {
        this(objSupplier, poolSize, 2, 10);
    }

    public ReusableObjPool(Supplier<T> objSupplier, int poolSize,
                           int growthFactor, int thresholdFactor) {
        this.objSupplier = objSupplier;

        this.initialPoolSize = poolSize;
        this.poolSize = poolSize;

        this.growthFactor = growthFactor;
        this.thresholdFactor = thresholdFactor;

        this.queue = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < poolSize; i++) {
            queue.offer(objSupplier.get());
        }
    }

    public T get() {
        if (possibleMemoryLeak) {
            return objSupplier.get();
        }

        T t = queue.poll();
        if (t != null) {
            return reset(t);
        }

        for (;;) {
            if (capacityCheckCAS.get() == 0) {
                try {
                    if (capacityCheckCAS.compareAndSet(0, 1)) {
                        if (queue == null) { //can be in case increaseCapacity fails
                            t = objSupplier.get();
                            break;
                        }

                        if ((t = queue.poll()) == null) {
                            increaseCapacity();
                            t = queue == null ? objSupplier.get() : queue.poll();
                        }
                        break;
                    }
                } finally {
                    capacityCheckCAS.compareAndSet(1, 0);
                }
            }
        }

        return reset(t);
    }

    private T reset(T t) {
        if (t instanceof ReusableObjPool.Reusable) {
            ((Reusable) t).reset();
        }
        return t;
    }

    private void increaseCapacity() {

        int oldSize = poolSize;
        int newSize = poolSize * growthFactor;

        if (newSize >= (thresholdFactor) * initialPoolSize) {
            //possible memory leak - revert to simply returning new objects so that
            //they are gc'ed and nullify this queue
            possibleMemoryLeak = true;
            queue = null;
            poolSize = 0;
            log.warn("Possible memory leak detected for " + objSupplier.get().getClass());
            return;
        }

        for (int i = 0; i < (newSize - oldSize); i++) {
            queue.offer(objSupplier.get());
        }
        poolSize = newSize;
    }

    public void release(T obj) {
        if (!possibleMemoryLeak && obj != null) {
            queue.offer(obj);
        }
    }

    public int poolSize() {
        return poolSize;
    }

    interface Reusable {
        void reset();
    }

}
