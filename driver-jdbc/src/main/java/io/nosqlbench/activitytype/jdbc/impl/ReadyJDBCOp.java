package io.nosqlbench.activitytype.jdbc.impl;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;

import java.util.function.LongFunction;

public class ReadyJDBCOp implements LongFunction<String> {
    private final StringBindings bindings;

    public ReadyJDBCOp(OpTemplate stmtDef) {
        ParsedTemplate paramTemplate = new ParsedTemplate(stmtDef.getStmt(), stmtDef.getBindings());
        BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
        StringBindingsTemplate template = new StringBindingsTemplate(stmtDef.getStmt(), paramBindings);

        bindings = template.resolve();
    }

    @Override
    public String apply(long cycle) {
        return bindings.bind(cycle);
    }
}
