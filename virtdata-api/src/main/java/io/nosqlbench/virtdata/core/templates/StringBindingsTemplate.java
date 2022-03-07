package io.nosqlbench.virtdata.core.templates;

//import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
//import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;

import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

/**
 * Uses a string template and a bindings template to create instances of {@link StringBindings}.
 */
public class StringBindingsTemplate {

    private final String stringTemplate;
    private final BindingsTemplate bindingsTemplate;

    public StringBindingsTemplate(String stringTemplate, BindingsTemplate bindingsTemplate) {
        this.stringTemplate = stringTemplate;
        this.bindingsTemplate = bindingsTemplate;
    }

    /**
     * Create a new instance of {@link StringBindings}, preferably in the thread context that will use it.
     * @return a new StringBindings
     */
    public StringBindings resolve() {
        return new StringBindings(stringTemplate,bindingsTemplate);
    }

    @Override
    public String toString() {
        return "TEMPLATE:"+this.stringTemplate+" BINDING:"+bindingsTemplate.toString();
    }
}
