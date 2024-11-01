package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.instruments.CqlOpMetrics;

public class Cqld4CqlOpImpl extends Cqld4CqlOp {
    public Cqld4CqlOpImpl(CqlSession session, int maxPages, boolean retryReplace, int maxLwtRetries, RSProcessors processors, CqlOpMetrics metrics) {
        super(session, maxPages, retryReplace, maxLwtRetries, processors, metrics);
    }

    @Override
    public Statement<?> getStmt() {
        return null;
    }

    @Override
    public String getQueryString() {
        return "";
    }
}
