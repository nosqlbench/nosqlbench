package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

import java.util.function.Function;

/**
 * Run a function on the current cached result and replace it
 * with the result of the function. Functions are one way of invoking
 * logic within a cycle. However, they are not intended to stand alone.
 * A CycleFunction must always have an input to work on. This input is
 * provided by a Supplier as optionally implemented by an Op
 *
 * @param <I> Some input type.
 * @param <O> Some output type.
 */
public interface ChainingOp<I,O> extends Op, Function<I,O> {
    @Override
    O apply(I i);
}
