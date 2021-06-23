package io.nosqlbench.activitytype.jdbc.impl;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;

public class ReadyJDBCOp implements OpDispenser<String> {
    private final StringBindings bindings;

    public ReadyJDBCOp(OpTemplate stmtDef) {
        ParsedTemplate paramTemplate = new ParsedTemplate(stmtDef.getStmt().orElseThrow(), stmtDef.getBindings());
        BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
        StringBindingsTemplate template = new StringBindingsTemplate(stmtDef.getStmt().orElseThrow(), paramBindings);

        bindings = template.resolve();
    }

    @Override
    public String apply(long cycle) {
        return bindings.bind(cycle);
    }
}
