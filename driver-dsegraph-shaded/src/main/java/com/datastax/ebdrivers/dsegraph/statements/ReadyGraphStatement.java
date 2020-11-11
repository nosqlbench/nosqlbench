package com.datastax.ebdrivers.dsegraph.statements;

import com.datastax.driver.dse.graph.SimpleGraphStatement;
import io.nosqlbench.virtdata.core.bindings.ContextualBindings;

public class ReadyGraphStatement implements BindableGraphStatement {

    private ContextualBindings<SimpleGraphStatement, SimpleGraphStatement> contextualBindings;

    public ReadyGraphStatement(ContextualBindings<SimpleGraphStatement, SimpleGraphStatement> contextualBindings) {
        this.contextualBindings = contextualBindings;
    }

    @Override
    public SimpleGraphStatement bind(long value) {
        return contextualBindings.bind(value);
    }
}
