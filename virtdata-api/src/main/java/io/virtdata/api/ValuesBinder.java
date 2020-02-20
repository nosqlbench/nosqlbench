package io.virtdata.api;

import io.virtdata.core.Bindings;

/**
 * <p>ValuesBinder provides a way to apply an map of named object values to a template
 * object of type T to yield a new object instance of type R. Get the values you need
 * from the bindings in any appropriate way and apply them to your template object.
 *
 * <p>Parameter Examples:</p>
 * <ul>
 *     <LI>T: prepared Statement, R: bound statement</LI>
 *     <LI>T: string template, R: interpolated string value</LI>
 * </ul>
 *
 * @param <T> The template type
 * @param <R> The result type
 */
public interface ValuesBinder<T, R> {

    /**
     * Using context instance of type S, AKA the template, create and bind values to
     * target object of type R
     * @param context A context object that knows how to provide an instance of type R
     * @param bindings A Bindings instance from which to draw the values
     * @param cycle The cycle for which to generate the values
     * @return The new result instance of R
     */
    R bindValues(T context, Bindings bindings, long cycle);
}
