package io.nosqlbench.engine.api.activityimpl;

public interface OpResultTracker {
    void onSuccess(long cycleValue, long nanoTime, long resultsize);
    void onError(long cycleValue, long resultNanos, Throwable t);
}
