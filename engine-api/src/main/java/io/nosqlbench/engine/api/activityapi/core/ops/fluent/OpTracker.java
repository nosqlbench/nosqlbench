package io.nosqlbench.engine.api.activityapi.core.ops.fluent;

import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.OpEvents;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;

import java.util.function.LongFunction;

public interface OpTracker<D> extends OpEvents<D> {

    void setMaxPendingOps(int maxPendingOps);
    int getMaxPendingOps();

    boolean isFull();
    int getPendingOps();

    void setCycleOpFunction(LongFunction<D> newOpFunction);

    // By making the op tracker the factory for ops, we allow it to hook their event streams
    TrackedOp<D> newOp(long cycle, OpEvents<D> strideTracker);

    boolean awaitCompletion(long timeout);
}
