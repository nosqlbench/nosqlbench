package io.nosqlbench.engine.api.activityapi.core.ops.fluent;

import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.OpEvents;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;

import java.util.function.LongFunction;

/**
 * The Op Tracker is the keeper of concurrency and op states. It serves a couple
 * key functions during the execution of an activity.
 * <OL>
 *     <LI>It provides a single control point for tracking the state of all operations
 *     for an activity.</LI>
 *     <LI>It provides a synchronization object for parameter updates which might affect
 *     whether new operations should block callers.</LI>
 * </OL>
 * @param <D>
 */
public interface OpTracker<D> extends OpEvents<D> {

    /**
     * The cycle op function is the function which can map a cycle number into
     * an operation of some sort.
     * @param newOpFunction
     */
    void setCycleOpFunction(LongFunction<D> newOpFunction);

    /**
     * The maximum pending ops determines how many ops an activity is allowed to have in
     * flight at any one time. When
     * @return
     */
    int getMaxPendingOps();
    void setMaxPendingOps(int maxPendingOps);

    boolean isFull();

    int getPendingOps();

    // By making the op tracker the factory for ops, we allow it to hook their event streams
    TrackedOp<D> newOp(long cycle, OpEvents<D> strideTracker);

    boolean awaitCompletion(long timeout);
}
