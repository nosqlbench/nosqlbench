package io.nosqlbench.engine.api.activityapi.planning;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;

import java.util.function.LongFunction;

/**
 * An OpSource provides an Op for a given long value.
 * OpSources are expected to be deterministic with respect to inputs.
 *
 * @param <T>
 */
public interface OpSource<T> extends LongFunction<T> {

    static <O extends Op> OpSource<O> of(OpSequence<OpDispenser<O>> seq) {
        return (long l) -> seq.apply(l).apply(l);
    }

    /**
     * Get the next operation for the given long value. This is simply
     * the offset indicated by the offset sequence array at a modulo
     * position.
     *
     * @param selector the long value that determines the next op
     * @return An op of type T
     */
    T get(long selector);

    @Override
    default T apply(long value) {
        return get(value);
    }
}
