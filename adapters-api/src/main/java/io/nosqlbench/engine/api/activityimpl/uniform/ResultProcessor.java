package io.nosqlbench.engine.api.activityimpl.uniform;

/**
 * A result processor can consume data from a result which is contains of a set of
 * ordered elements.
 * @param <C> The result type which acts as the container of elements
 * @param <E> The element type
 */
public interface ResultProcessor<C,E> {

    /**
     * Call the start method before any buffering of results.
     * @param cycle The cycle that the result is associated with. This is for presentation only.
     * @param container The result object which holds individual result elements.
     */
    void start(long cycle, C container);

    /**
     * For each element in the container, buffer it. The effect of buffering is contextual. For
     * ResultProcessors which need to see all the result data before applying its effect,
     * simply buffer the elements to an internal container type.
     * @param element
     */
    void buffer(E element);

    /**
     * Once all the elements of the result have been buffered, flush must be called.
     * ResultProcessors which need to see all the data can finish processing here.
     */
    void flush();
}
