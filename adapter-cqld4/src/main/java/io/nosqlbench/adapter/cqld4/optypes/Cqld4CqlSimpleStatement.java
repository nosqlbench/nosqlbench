package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.RSProcessors;

public class Cqld4CqlSimpleStatement extends Cqld4CqlOp {
    private final SimpleStatement stmt;

    public Cqld4CqlSimpleStatement(CqlSession session, SimpleStatement stmt, int maxpages, boolean retryreplace) {
        super(session, maxpages,retryreplace, new RSProcessors());
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
