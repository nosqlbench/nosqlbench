package io.nosqlbench.virtdata.api.templates;

import io.nosqlbench.virtdata.api.Bindings;
import io.nosqlbench.virtdata.api.BindingsTemplate;

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
