package io.virtdata.core;

import io.virtdata.api.Binder;
import io.virtdata.api.ValuesMapBinder;

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
    private Bindings bindings;
    private ValuesMapBinder<C, R> valuesMapBinder;

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
