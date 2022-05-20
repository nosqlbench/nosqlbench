package io.nosqlbench.virtdata.core.bindings;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
