package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.statements.core.ReadyCQLStatement;
import com.google.common.util.concurrent.FutureCallback;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;

public class CqlOpData implements FutureCallback<ResultSet> {
    final long cycle;

    // op state is managed via callbacks, we keep a ref here
    StartedOp<CqlOpData> startedOp;

    boolean skipped=false;
    private CqlAsyncAction action;
    int triesAttempted =0;

    ReadyCQLStatement readyCQLStatement;
    Statement statement;
    ResultSetFuture future;
    ResultSet resultSet;

    long totalRowsFetchedForQuery;
    long totalPagesFetchedForQuery;

    public Throwable throwable;
    public long resultAt;
    private long errorAt;

    public CqlOpData(long cycle, CqlAsyncAction action) {
        this.cycle = cycle;
        this.action = action;
    }

    @Override
    public void onSuccess(ResultSet result) {
        this.resultSet = result;
        this.resultAt = System.nanoTime();
        action.onSuccess(startedOp);

    }

    @Override
    public void onFailure(Throwable throwable) {
        this.throwable=throwable;
        this.errorAt = System.nanoTime();
        action.onFailure(startedOp);
    }

}
