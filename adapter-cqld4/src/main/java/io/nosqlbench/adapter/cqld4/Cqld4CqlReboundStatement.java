package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;

public class Cqld4CqlReboundStatement extends Cqld4CqlOp {
    private final BoundStatement stmt;

    public Cqld4CqlReboundStatement(CqlSession session, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics, BoundStatement rebound, RSProcessors processors) {
        super(session,maxpages,retryreplace,metrics,processors);
        this.stmt = rebound;
    }

    @Override
    public Statement<?> getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        return stmt.getPreparedStatement().getQuery();
    }
}
