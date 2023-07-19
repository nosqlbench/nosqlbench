/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.Cqld4CqlReboundStatement;
import io.nosqlbench.adapter.cqld4.LWTRebinder;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.exceptions.ChangeUnappliedCycleException;
import io.nosqlbench.adapter.cqld4.exceptions.ExceededRetryReplaceException;
import io.nosqlbench.adapter.cqld4.exceptions.UnexpectedPagingException;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;


// TODO: add statement filtering
// TODO: add statement post processing for trace capture
// TODO: add trace capture
// TODO: add/document max tries exhausted exception
// TODO: add/document UnexpectedPagingException
// TODO: add/document chnge unapplied exception
// TODO: add total rows metrics
// TODO: add rows histogram resultSetSizeHisto


public abstract class Cqld4CqlOp implements CycleOp<List<Row>>, VariableCapture, OpGenerator, OpResultSize {

    private final CqlSession session;
    private final int maxPages;
    private final boolean retryReplace;
    private final int maxLwtRetries;
    private final RSProcessors processors;
    private final ThreadLocal<List<Row>> results = new ThreadLocal<>();
    private int retryReplaceCount = 0;
    private Cqld4CqlOp nextOp;

    public Cqld4CqlOp(CqlSession session, int maxPages, boolean retryReplace, int maxLwtRetries, RSProcessors processors) {
        this.session = session;
        this.maxPages = maxPages;
        this.retryReplace = retryReplace;
        this.maxLwtRetries = maxLwtRetries;
        this.processors = processors;
    }

    protected Cqld4CqlOp(CqlSession session, int maxPages, boolean retryReplace, int maxLwtRetries,
                         int retryRplaceCount, RSProcessors processors) {
        this.session = session;
        this.maxPages = maxPages;
        this.retryReplace = retryReplace;
        this.maxLwtRetries = maxLwtRetries;
        this.retryReplaceCount = retryRplaceCount;
        this.processors = processors;
    }

    public final List<Row> apply(long cycle) {

        final Statement<?> stmt = getStmt();
        final CompletionStage<AsyncResultSet> statementStage = session.executeAsync(stmt);
        final List<Row> completeRowSet = new ArrayList<>();
        statementStage.whenComplete(
            (rs, ex) -> {
                if (ex != null) {
                    throw new RuntimeException("Failed to obtain statement result set.", ex);
                }
                processors.start(cycle, rs);

                final AtomicInteger totalRows = new AtomicInteger();
                final AtomicInteger pages = new AtomicInteger(1);

                if (!rs.wasApplied()) {
                    if (!retryReplace) {
                        throw new ChangeUnappliedCycleException(rs, getQueryString());
                    } else {
                        retryReplaceCount++;
                        if (retryReplaceCount > maxLwtRetries) {
                            throw new ExceededRetryReplaceException(rs, getQueryString(), retryReplaceCount);
                        }
                        Row one = rs.one();
                        processors.buffer(one);
                        totalRows.getAndIncrement();
                        nextOp = this.rebindLwt(stmt, one);
                    }
                }
                completeRowSet.addAll(captureCurrentPage(rs));

                while (rs.hasMorePages()) {

                    if (pages.getAndIncrement() > maxPages) {
                        throw new UnexpectedPagingException(rs, getQueryString(), pages.get(), maxPages,
                            stmt.getPageSize());
                    }

                    final CompletionStage<AsyncResultSet> pagedStage = rs.fetchNextPage();
                    pagedStage.whenComplete(
                        (rsPaged, err) -> {
                            if (err != null) {
                                throw new RuntimeException("Failed to obtain paged result set", err);
                            }
                            completeRowSet.addAll(captureCurrentPage(rsPaged));
                        });
                }
            });

        results.set(completeRowSet);
        processors.flush();

        return results.get();
    }


    private List<Row> captureCurrentPage(AsyncResultSet rs) {
        Iterable<Row> rows = rs.currentPage();
        List<Row> resultRows = new ArrayList<>();
        if (rows.iterator().hasNext()) {
            for (Row row : rows) {
                processors.buffer(row);
                resultRows.add(row);
            }
        }
        return resultRows;
    }

    @Override
    public Op getNextOp() {
        Op next = nextOp;
        nextOp = null;
        return next;
    }

    public Map<String, ?> capture() {
        throw new NotImplementedException("Not implemented for Cqld4CqlOp");
    }

    public abstract Statement<?> getStmt();

    public abstract String getQueryString();

    private Cqld4CqlOp rebindLwt(Statement<?> stmt, Row row) {
        BoundStatement rebound = LWTRebinder.rebindUnappliedStatement(stmt, row);
        return new Cqld4CqlReboundStatement(session, maxPages, retryReplace, maxLwtRetries, retryReplaceCount, rebound, processors);
    }

}
