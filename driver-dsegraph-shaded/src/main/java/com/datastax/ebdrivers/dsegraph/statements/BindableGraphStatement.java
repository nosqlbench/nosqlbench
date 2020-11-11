package com.datastax.ebdrivers.dsegraph.statements;

import com.datastax.driver.dse.graph.SimpleGraphStatement;

public interface BindableGraphStatement {
    SimpleGraphStatement bind(long value);
}
