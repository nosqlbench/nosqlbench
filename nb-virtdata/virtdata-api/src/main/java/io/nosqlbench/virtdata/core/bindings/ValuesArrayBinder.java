/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.core.templates.StringCompositor;

/**
 * <p>ValuesArrayBinder provides a way to apply an array of object values to a template
 * object of type T to yield a new object instance of type R. The object array is
 * a positional argument list. There is no named-argument facility.</p>
 *
 * <p>Parameter Examples:</p>
 * <ul>
 *     <LI>T: prepared Statement, R: bound statement</LI>
 *     <LI>T: string template, R: interpolated string value</LI>
 * </ul>
 *
 * <p>ValuesArrayBinders can either be created as helper types, to be passed in as
 * mapping functions to other calls, or they can be directly implemented in higher-order
 * types which include the ability to produce objects of type R from values provided.
 * Both types of use are found in this API. An example of the former type would be
 * {@link ContextualBindingsArrayTemplate},
 * while and example of the latter would
 * be {@link StringCompositor}.
 * be {@link StringCompositor}.
 * </p>
 *
 * @param <T> The template type
 * @param <R> The result type
 */
public interface ValuesArrayBinder<T, R> {

    /**
     * Using context instance of type T, AKA the template, create and bind values to
     * target object of type R
     * @param context A context object that knows how to provide an instance of type R
     * @param values An array of values which should be bound to the new R instance
     * @return The new result instance of R
     */
    R bindValues(T context, Object[] values);
}
