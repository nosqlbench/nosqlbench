package io.nosqlbench.adapter.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.driver.cqld4.Cqld4Op;

public class Cqld4SimpleCqlStatement extends Cqld4Op {
    private final CqlSession session;
    private final SimpleStatement stmt;

    public Cqld4SimpleCqlStatement(CqlSession session, SimpleStatement stmt) {
        this.session = session;
        this.stmt = stmt;
    }

    @Override
    public void run() {
        session.execute(stmt);
    }
}
