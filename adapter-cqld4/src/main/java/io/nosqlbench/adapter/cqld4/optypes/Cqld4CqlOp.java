package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.*;
import io.nosqlbench.adapter.cqld4.exceptions.ChangeUnappliedCycleException;
import io.nosqlbench.adapter.cqld4.exceptions.UndefinedResultSetException;
import io.nosqlbench.adapter.cqld4.exceptions.UnexpectedPagingException;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.OpGenerator;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.VariableCapture;

import java.util.Iterator;
import java.util.Map;


// TODO: add statement filtering
// TODO: add statement pre and post processing for trace capture and start timer op
// TODO: add trace capture
// TODO: add start timer op
// TODO: add stop timer op
// TODO: add showcql equivalent
// TODO: add/document max tries exhausted exception
// TODO: add/document UnexpectedPagingException
// TODO: add/document chnge unapplied exception
// TODO: add instrumented metrics
// TODO: add total rows metrics
// TODO: add rows histogram resultSetSizeHisto


public abstract class Cqld4CqlOp implements CycleOp<ResultSet>, VariableCapture, OpGenerator {

    private final CqlSession session;
    private final int maxpages;
    private final boolean retryreplace;
    private final Cqld4OpMetrics metrics;

    private ResultSet rs;
    private Cqld4CqlOp nextOp;
    private final RSProcessors processors;

    public Cqld4CqlOp(CqlSession session, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics) {
        this.session = session;
        this.maxpages = maxpages;
        this.retryreplace = retryreplace;
        this.processors = new RSProcessors();
        this.metrics = metrics;
    }

    public Cqld4CqlOp(CqlSession session, int maxpages, boolean retryreplace, Cqld4OpMetrics metrics, RSProcessors processors) {
        this.session = session;
        this.maxpages = maxpages;
        this.retryreplace = retryreplace;
        this.processors = processors;
        this.metrics = metrics;
    }

    public final ResultSet apply(long cycle) {

        metrics.onStart();
        Statement<?> stmt = getStmt();

        rs = session.execute(stmt);

        processors.start(cycle, rs);

        int totalRows=0;

        if (!rs.wasApplied()) {
            if (!retryreplace) {
                throw new ChangeUnappliedCycleException(rs, getQueryString());
            } else {
                Row one = rs.one();
                processors.buffer(one);
                totalRows++;
                nextOp = this.rebindLwt(stmt, one);
            }
        }

        // Paginated Op

        Iterator<Row> reader = rs.iterator();
        int pages = 0;
        while (true) {
            int pageRows = rs.getAvailableWithoutFetching();
            for (int i = 0; i < pageRows; i++) {
                Row row = reader.next();
                processors.buffer(row);
            }
            if (pages++ > maxpages) {
                throw new UnexpectedPagingException(rs, getQueryString(), pages, maxpages, stmt.getPageSize());
            }
            if (rs.isFullyFetched()) {
                break;
            }
            totalRows+=pageRows;
        }
        processors.flush();
        metrics.onSuccess();
        return rs;
    }

    @Override
    public Op getNextOp() {
        Op next = nextOp;
        nextOp = null;
        return next;
    }

    @Override
    public Map<String, ?> capture() {
        if (rs == null) {
            throw new UndefinedResultSetException(this);
        }
        return null;
    }

    public abstract Statement<?> getStmt();

    public abstract String getQueryString();

    private Cqld4CqlOp rebindLwt(Statement<?> stmt, Row row) {
        BoundStatement rebound = LWTRebinder.rebindUnappliedStatement(stmt, row);
        return new Cqld4CqlReboundStatement(session,maxpages,retryreplace,metrics,rebound,processors);
    }

}
