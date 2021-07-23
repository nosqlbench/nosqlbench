package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;

public class Cqld4PreparedStatement extends Cqld4Op {

    private final CqlSession session;
    private final BoundStatement stmt;

    public Cqld4PreparedStatement(CqlSession session, BoundStatement stmt) {
        this.session = session;
        this.stmt = stmt;
    }

    @Override
    public void run() {
        session.execute(stmt);
    }
}
