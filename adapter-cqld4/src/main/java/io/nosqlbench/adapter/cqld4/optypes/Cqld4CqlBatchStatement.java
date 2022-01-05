package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;

public class Cqld4CqlBatchStatement extends Cqld4CqlOp {

    private final BatchStatement stmt;

    public Cqld4CqlBatchStatement(CqlSession session, BatchStatement stmt, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics) {
        super(session,maxpages,retryreplace,metrics);
        this.stmt = stmt;
    }

    @Override
    public BatchStatement getStmt() {
        return stmt;
    }

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        stmt.iterator().forEachRemaining(s -> sb.append(s).append("\n"));
        return sb.toString();
    }
}
