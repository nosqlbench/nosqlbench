/*
 * Copyright (c) 2022 nosqlbench
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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A thread-local template that describes a set of data mappers, a context object, and a method for applying
 * mapped values to the context object via a String-Object map. This type is used in thread-local scope to map thread-specific
 * data mapper instances to a contextual template object and a method for applying mapped values to it.</p>
 *
 * <p>This type is generally constructed by a ContextualBindingsTemplate.</p>
 *
 * @param <C> The type of the contextual template object.
 * @param <R> The resulting type from binding mapped values with the contextual template C
 */
public class ContextualMapBindings<C, R> implements Binder<R> {

    private final C context;
    private final Bindings bindings;
    private final ValuesMapBinder<C, R> valuesMapBinder;

    public ContextualMapBindings(Bindings bindings, C context, ValuesMapBinder<C, R> valuesMapBinder) {
        this.bindings = bindings;
        this.context = context;
        this.valuesMapBinder = valuesMapBinder;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public C getContext() {
        return context;
    }

    @Override
    public R bind(long value) {
        Map<String,Object> generatedValues = new HashMap<String,Object>();
        bindings.setMap(generatedValues, value);
        try { // Provide bindings context data where it may be useful
            return valuesMapBinder.bindValues(context, generatedValues);
        } catch (Exception e) {
            throw new RuntimeException("Binding error:" + bindings.getTemplate().toString() + ": " + generatedValues, e);
        }

    }
}
