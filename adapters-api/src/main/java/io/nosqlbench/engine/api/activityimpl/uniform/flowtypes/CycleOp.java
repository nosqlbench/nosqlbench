package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

import java.util.function.LongFunction;

/**
 * A CycleRunnable is simply a variation of a Runnable type.
 * The main difference is that it is supplied with the cycle
 * as input.
 */
public interface CycleOp<T> extends Op, LongFunction<T> {
//    /**
//     * <p>Run an action for the given cycle. The cycle is provided for anecdotal
//     * usage such as logging and debugging. It is valid to use the cycle value in these places,
//     * but you should not use it to determine the logic of what is run. The mechanism
//     * for doing this is provided in {@link io.nosqlbench.engine.api.activityimpl.OpMapper}
//     * and {@link io.nosqlbench.engine.api.activityimpl.OpDispenser} types.</p>
//     *
//     *
//     * @param cycle The cycle value for which an operation is run
//     */
////     * This method should do the same thing that {@link #apply(long)} does, except that
////     * there is no need to prepare or return a result. This is the form that will be called
////     * if there is no chaining operation to consume the result of this operation.
//    void accept(long cycle);

    /**
     * <p>Run an action for the given cycle. The cycle
     * value is only to be used for anecdotal presentation. This form is called
     * when there is a chaining operation which will do something with this result.</p>
     * @param value The cycle value for which an operation is run
     * @return A result which is the native result type for the underlying driver.
     */
    @Override
    T apply(long value);


}
