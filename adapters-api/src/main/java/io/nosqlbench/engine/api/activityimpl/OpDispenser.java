package io.nosqlbench.engine.api.activityimpl;

import java.util.function.LongFunction;

/**
 * <p>
 * <H2>Synopsis</H2>
 * An OpDispenser is responsible for mapping a cycle number into
 * an executable operation. This is where <i>Op Synthesis</i> occurs
 * in NoSQLBench -- The process of building executable operations from
 * templates.</p>
 * <hr/>
 * <p>
 * <H2>Concepts</H2>
 * Op Synthesis is the process of building a specific and executable
 * operation for some (low level driver) API by combining the
 * static and dynamic elements of the operation together.
 * In most cases, implementations of OpDispenser will be constructed
 * within the logic of an {@link OpMapper} which is responsible for
 * determine the type of OpDispenser to use as associated with a specific
 * type {@code (<T>)}.
 * </p>
 *
 * @param <T> The parameter type of the actual operation which will be used
 *            to hold all the details for executing an operation,
 *            generally something that implements {@link Runnable}.
 */
public interface OpDispenser<T> extends LongFunction<T> {
@Override
    T apply(long value);
}
