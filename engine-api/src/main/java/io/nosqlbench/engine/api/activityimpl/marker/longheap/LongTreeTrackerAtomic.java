package io.nosqlbench.engine.api.activityimpl.marker.longheap;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a version of longTreeTracker that is safe for concurrent access.
 */
public class LongTreeTrackerAtomic extends LongTreeTracker {
    private AtomicLong timage = new AtomicLong(0L);

    @Override
    public long setCompleted(long index) {
        long before = timage.get();
        long after = super.setCompleted(index, before);
        while (!timage.compareAndSet(before,after)) {
            before = timage.get();
            after = super.setCompleted(index, before);
        }
        return after;
    }
}
