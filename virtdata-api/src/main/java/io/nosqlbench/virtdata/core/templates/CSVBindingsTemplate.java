package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

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
