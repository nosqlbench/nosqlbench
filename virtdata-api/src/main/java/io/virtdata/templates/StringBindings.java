package io.virtdata.templates;

import io.virtdata.api.Binder;
import io.virtdata.core.Bindings;

/**
 * Allows the generation of strings from a string template and bindings template.
 */
public class StringBindings implements Binder<String> {

    private final StringCompositor compositor;
    private Bindings bindings;

    public StringBindings(StringCompositor compositor, Bindings bindings) {
        this.compositor = compositor;
        this.bindings = bindings;
    }

    /**
     * Call the data mapper bindings, assigning the returned values positionally to the anchors in the string binding.
     * @param value a long input value
     * @return a new String containing the mapped values
     */
    @Override
    public String bind(long value) {
        String s = compositor.bindValues(compositor,bindings,value);
        return s;
    }
}
