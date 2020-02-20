package io.virtdata.templates;

import io.virtdata.core.Bindings;
import io.virtdata.core.BindingsTemplate;

import java.util.HashSet;

/**
 * Uses a string template and a bindings template to create instances of {@link StringBindings}.
 */
public class StringBindingsTemplate {

    private String stringTemplate;
    private BindingsTemplate bindingsTemplate;

    public StringBindingsTemplate(String stringTemplate, BindingsTemplate bindingsTemplate) {
        this.stringTemplate = stringTemplate;
        this.bindingsTemplate = bindingsTemplate;
    }

    /**
     * Create a new instance of {@link StringBindings}, preferably in the thread context that will use it.
     * @return a new StringBindings
     */
    public StringBindings resolve() {

        StringCompositor compositor = new StringCompositor(stringTemplate);
        HashSet<String> unqualifiedNames = new HashSet<>(compositor.getBindPointNames());
        unqualifiedNames.removeAll(new HashSet<>(bindingsTemplate.getBindPointNames()));
        if (unqualifiedNames.size()>0) {
            throw new RuntimeException("Named anchors were specified in the template which were not provided in the bindings: " + unqualifiedNames.toString());
        }

        Bindings bindings = bindingsTemplate.resolveBindings();
        return new StringBindings(compositor,bindings);
    }
}
