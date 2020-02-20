package io.virtdata.templates;

import io.virtdata.core.Bindings;
import io.virtdata.core.BindingsTemplate;

public class CSVBindingsTemplate {

    private BindingsTemplate bindingsTemplate;

    public CSVBindingsTemplate(BindingsTemplate bindingsTemplate) {
        this.bindingsTemplate = bindingsTemplate;
    }

    public CSVBindings resolve() {
        Bindings bindings = bindingsTemplate.resolveBindings();
        return new CSVBindings(bindings);
    }
}
