package io.virtdata.core;

import io.virtdata.api.Binder;
import io.virtdata.api.ValuesBinder;

/**
 * <p>A thread-local template that describes a set of data mappers, a context object, and a method for applying
 * mapped values to the context object directly from the bindings. This type is used in thread-local scope
 * to map thread-specific data mapper instances to a contextual template object and a method for applying mapped values to it.</p>
 *
 * <p>This type is generally constructed by a ContextualDirectBindingsTemplate.</p>
 *
 * @param <C> The type of the contextual template object.
 * @param <R> The resulting type from binding mapped values with the contextual template C
 */
public class ContextualBindings<C, R> implements Binder<R> {

    private final C context;
    private Bindings bindings;
    private ValuesBinder<C, R> valuesBinder;

    public ContextualBindings(Bindings bindings, C context, ValuesBinder<C, R> valuesBinder) {
        this.bindings = bindings;
        this.context = context;
        this.valuesBinder = valuesBinder;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public C getContext() {
        return context;
    }

    @Override
    public R bind(long value) {
        Object[] allGeneratedValues = bindings.getAll(value);
        try { // Provide bindings context data where it may be useful
            return valuesBinder.bindValues(context, bindings, value);
        } catch (Exception e) {
            throw new RuntimeException("Binding error:" + bindings.getTemplate().toString(allGeneratedValues), e);
        }

    }
}
