package io.nosqlbench.virtdata.api;

/***
 * A Binder is a type that knows how to return a result object given a long value
 * to bind mapped values with.
 * @param <R> The resulting object type
 */
public interface Binder<R> {
    /**
     * Bind values derived from a long to some object, returning an object type R
     * @param value a long input value
     * @return an R
     */
    R bind(long value);
}
