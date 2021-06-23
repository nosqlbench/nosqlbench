package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.Binder;
import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

/**
 * Allows the generation of strings from a string template and bindings template.
 */
public class StringBindings implements Binder<String> {

    private final StringCompositor compositor;
    private final Bindings bindings;

    public StringBindings(StringCompositor compositor, Bindings bindings) {
        this.compositor = compositor;
        this.bindings = bindings;
    }

    public StringBindings(ParsedTemplate pt) {
        this.compositor = new StringCompositor(pt);
        this.bindings = new BindingsTemplate(pt.getBindPoints()).resolveBindings();
    }

    /**
     * Call the data mapper bindings, assigning the returned values positionally to the anchors in the string binding.
     *
     * @param value a long input value
     * @return a new String containing the mapped values
     */
    @Override
    public String bind(long value) {
        String s = compositor.bindValues(compositor, bindings, value);
        return s;
    }

    @Override
    public String toString() {
        return "StringBindings{" +
                "compositor=" + compositor +
                ", bindings=" + bindings +
                '}';
    }
}
