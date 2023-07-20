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
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.*;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


// TODO: add statement filtering
// TODO: add statement post processing for trace capture
// TODO: add trace capture
// TODO: add/document max tries exhausted exception
// TODO: add/document UnexpectedPagingException
// TODO: add/document chnge unapplied exception
// TODO: add total rows metrics
// TODO: add rows histogram resultSetSizeHisto


public abstract class Cqld4CqlOp implements CycleOp<List<Row>>, VariableCapture, OpGenerator, OpResultSize {
    private final static Logger logger = LogManager.getLogger(Cqld4CqlOp.class);

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

        Statement<?> statement = getStmt();
        List<Row> completeRowSet = new ArrayList<>();
        logger.debug("apply() invoked, statement obtained, executing async with page size: " + statement.getPageSize() + " thread local rows: ");

        CompletionStage<AsyncResultSet> statementStage = session.executeAsync(statement);
        CompletionStage<List<Row>> collectedRowsFuture = statementStage.thenCompose
            (rs -> {
                if (!rs.wasApplied()) {
                    handleRebindLWT(rs, statement);
                }
                return collect(rs, completeRowSet, cycle);
            });

        collectedRowsFuture.whenComplete((rs, ex) -> {
            if (ex != null) {
                throw new RuntimeException("Failed to obtain statement result set.", ex);
            }

            logger.info("\n\n--- Rows collected for cycle: " + cycle + " count: "
                + rs.size() + " dt: " + System.nanoTime());

            results.set(completeRowSet);
            processors.flush();
        });

        return results.get();
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

    private CompletionStage<List<Row>> collect(AsyncResultSet resultSet, List<Row> rowList, final long cycle) {

        final List<Row> rows = rowList;
        for (Row row : resultSet.currentPage()) {
            rows.add(row);
            processors.buffer(row);
        }

        if (resultSet.hasMorePages()) {
            return resultSet.fetchNextPage().thenCompose(rs -> collect(rs, rows, cycle));
        } else {
            processors.start(cycle, resultSet);
            return CompletableFuture.completedStage(rows);
        }
    }

    private void handleRebindLWT(AsyncResultSet resultSet, Statement<?> statement) {
        if (!retryReplace) {
            throw new ChangeUnappliedCycleException(resultSet, getQueryString());
        } else {
            retryReplaceCount++;
            if (retryReplaceCount > maxLwtRetries) {
                throw new ExceededRetryReplaceException(resultSet, getQueryString(), retryReplaceCount);
            }
            Row one = resultSet.one();
            processors.buffer(one);
            nextOp = this.rebindLwt(statement, one);
        }
    }

}
