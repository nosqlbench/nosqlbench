package io.nosqlbench.virtdata.api;

import java.util.Map;

/**
 * <p>ValuesMapBinder provides a way to apply an map of named object values to a template
 * object of type T to yield a new object instance of type R. The object array is
 * a positional argument list. There is no named-argument facility.</p>
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
public interface ValuesMapBinder<T, R> {

    /**
     * Using context instance of type S, AKA the template, create and bind values to
     * target object of type R
     * @param context A context object that knows how to provide an instance of type R
     * @param values An array of values which should be bound to the new R instance
     * @return The new result instance of R
     */
    R bindValues(T context, Map<String,Object> values);
}
