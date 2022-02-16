package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.Binder;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

import java.util.Map;

/**
 * Allows the generation of strings from a string template and bindings template.
 */
public class StringBindings implements Binder<String> {

    private final StringCompositor compositor;

    public StringBindings(String template) {
        this(template,Map.of(),Map.of());
    }

    public StringBindings(String template, Map<String, String> bindings) {
        this(template,bindings,Map.of());
    }

    public StringBindings(String template, Map<String,String> bindings, Map<String,Object> fconfig) {
        ParsedTemplate parsed = new ParsedTemplate(template,bindings);
        this.compositor = new StringCompositor(parsed, fconfig);
    }

    public StringBindings(ParsedTemplate parsedTemplate) {
        this(parsedTemplate, Map.of());
    }

    public StringBindings(ParsedTemplate pt, Map<String,Object> fconfig) {
        this.compositor = new StringCompositor(pt,fconfig);
    }

    public StringBindings(String stringTemplate, BindingsTemplate bindingsTemplate) {
        this(stringTemplate,bindingsTemplate.getMap());
    }

    /**
     * Call the data mapper bindings, assigning the returned values positionally to the anchors in the string binding.
     *
     * @param value a long input value
     * @return a new String containing the mapped values
     */
    @Override
    public String bind(long value) {
        return compositor.apply(value);
    }

    @Override
    public String toString() {
        return "StringBindings{" +
                "compositor=" + compositor +
                '}';
    }
}
