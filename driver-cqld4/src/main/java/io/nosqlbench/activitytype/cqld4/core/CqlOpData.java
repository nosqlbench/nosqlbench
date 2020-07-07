package io.nosqlbench.activitytype.cqld4.core;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.activitytype.cqld4.statements.core.ReadyCQLStatement;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CqlOpData {

    final long cycle;
//    public CompletionStage<AsyncResultSet> completionStage;

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

    public void handleAsyncResult(AsyncResultSet asyncResultSet, Throwable throwable) {
        if (throwable!=null) {
            this.throwable = throwable;
            this.errorAt = System.nanoTime();
            action.onFailure(startedOp);
        } else {
            this.page = asyncResultSet.currentPage();
            this.resultAt = System.nanoTime();
            action.onSuccess(startedOp, asyncResultSet);
        }
    }

}
