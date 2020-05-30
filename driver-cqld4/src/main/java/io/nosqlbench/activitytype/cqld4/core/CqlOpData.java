package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.statements.core.ReadyCQLStatement;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CqlOpData extends CompletableFuture<AsyncResultSet> {

    final long cycle;
    public CompletionStage<AsyncResultSet> completionStage;

    // op state is managed via callbacks, we keep a ref here
    StartedOp<CqlOpData> startedOp;

    boolean skipped=false;
    private CqlAsyncAction action;
    int triesAttempted =0;

    ReadyCQLStatement readyCQLStatement;
    Statement statement;

    long totalRowsFetchedForQuery;
    long totalPagesFetchedForQuery;

    public Throwable throwable;
    public long resultAt;
    private long errorAt;
    private Iterable<Row> page;

    public CqlOpData(long cycle, CqlAsyncAction action) {
        this.cycle = cycle;
        this.action = action;
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        this.throwable=ex;
        this.errorAt = System.nanoTime();
        action.onFailure(startedOp);
        return true;
    }

    @Override
    public boolean complete(AsyncResultSet value) {
        this.page = value.currentPage();
        this.resultAt = System.nanoTime();
        action.onSuccess(startedOp);
        return true;
        // ? return !value.hasMorePages();
    }

}
