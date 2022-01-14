package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

public class Cqld4ScriptGraphOp implements CycleOp<GraphResultSet> {
    private final CqlSession session;
    private final ScriptGraphStatement stmt;
    private int resultSize=0;

    public Cqld4ScriptGraphOp(CqlSession session, ScriptGraphStatement stmt) {
        this.session = session;
        this.stmt = stmt;
    }

    @Override
    public GraphResultSet apply(long value) {
        GraphResultSet result = session.execute(stmt);
        this.resultSize = result.all().size();
        return result;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
