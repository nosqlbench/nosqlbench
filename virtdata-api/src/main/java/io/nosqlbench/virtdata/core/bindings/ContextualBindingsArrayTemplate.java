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
 * A template that maps a set of specifiers, a context object, and a method for applying
 * mapped values to the context object. This can be used in the configuration phase, in global
 * scope without triggering mapper bindings resolution from specifiers.
 *
 * @param <C> The type of the contextual template object.
 * @param <R> The type which will be produced when mapped values are applied to a type C
 */
public class ContextualBindingsArrayTemplate<C, R> {

    private C context;
    private BindingsTemplate bindingsTemplate;
    private ValuesArrayBinder<C, R> valuesArrayBinder;

    public ContextualBindingsArrayTemplate(C context,
                                           BindingsTemplate bindingsTemplate,
                                           ValuesArrayBinder<C, R> valuesArrayBinder) {
        this.context = context;
        this.bindingsTemplate = bindingsTemplate;
        this.valuesArrayBinder = valuesArrayBinder;
    }

    public C getContext() {
        return context;
    }

    public BindingsTemplate getBindingsTemplate() {
        return bindingsTemplate;
    }

    public ValuesArrayBinder<C, R> getValuesArrayBinder() {
        return valuesArrayBinder;
    }

    public ContextualArrayBindings<C, R> resolveBindings() {
        Bindings bindings = bindingsTemplate.resolveBindings();
        return new ContextualArrayBindings<C, R>(bindings, context, valuesArrayBinder);
    }

}
