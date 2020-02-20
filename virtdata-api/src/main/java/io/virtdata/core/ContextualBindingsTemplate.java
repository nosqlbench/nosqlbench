package io.virtdata.core;

import io.virtdata.api.ValuesBinder;

/**
 * A template that maps a set of specifiers, a context object, and a method for applying
 * mapped values to the context object. This can be used in the configuration phase, in global
 * scope without triggering mapper bindings resolution from specifiers.
 *
 * @param <C> The type of the contextual template object.
 * @param <R> The type which will be produced when mapped values are applied to a type C
 */
public class ContextualBindingsTemplate<C, R> {

    private C context;
    private BindingsTemplate bindingsTemplate;
    private ValuesBinder<C, R> valuesBinder;

    public ContextualBindingsTemplate(C context,
                                      BindingsTemplate bindingsTemplate,
                                      ValuesBinder<C, R> valuesMapBinder) {
        this.context = context;
        this.bindingsTemplate = bindingsTemplate;
        this.valuesBinder = valuesMapBinder;
    }

    public C getContext() {
        return context;
    }

    public BindingsTemplate getBindingsTemplate() {
        return bindingsTemplate;
    }

    public ValuesBinder<C, R> getValuesBinder() {
        return valuesBinder;
    }

    public ContextualBindings<C, R> resolveBindings() {
        Bindings bindings = bindingsTemplate.resolveBindings();
        return new ContextualBindings<C, R>(bindings, context, valuesBinder);
    }

}
