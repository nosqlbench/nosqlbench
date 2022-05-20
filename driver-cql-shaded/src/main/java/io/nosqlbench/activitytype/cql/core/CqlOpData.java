package io.nosqlbench.activitytype.cql.core;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.statements.core.ReadyCQLStatement;
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
