package io.nosqlbench.virtdata.core.bindings;

import java.util.function.LongFunction;

/***
 * A Binder is a type that knows how to return a result object given a long value
 * to bind mapped values with.
 * @param <R> The resulting object type
 */
public interface Binder<R> extends LongFunction<R> {
    /**
     * Bind values derived from a long to some object, returning an object type R
     * @param value a long input value
     * @return an R
     */
    R bind(long value);

    @Override
    default R apply(long value) {
        return bind(value);
    }
}
