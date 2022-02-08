package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;

public class Cqld4CqlSimpleStatement extends Cqld4CqlOp {
    private final SimpleStatement stmt;

    public Cqld4CqlSimpleStatement(CqlSession session, SimpleStatement stmt, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics) {
        super(session, maxpages,retryreplace,metrics);
        this.stmt = stmt;
    }

    @Override
    public SimpleStatement getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        return stmt.getQuery();
    }

}
