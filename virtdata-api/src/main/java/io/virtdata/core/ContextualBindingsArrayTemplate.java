package io.virtdata.core;

import io.virtdata.api.ValuesArrayBinder;

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
