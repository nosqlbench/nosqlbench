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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.codahale.metrics.Histogram;
import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metadata.token.Token;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.instruments.CqlOpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.function.LongFunction;

public abstract class Cqld4BaseOpDispenser extends BaseOpDispenser<Cqld4CqlOp, Cqld4Space> implements CqlOpMetrics {

    private final static Logger logger = LogManager.getLogger("CQLD4");

    private final int maxpages;
    private final LongFunction<CqlSession> sessionFunc;
    private final boolean isRetryReplace;
    private final int maxLwtRetries;
    private final Histogram rowsHistogram;
    private final Histogram pagesHistogram;
    private final Histogram payloadBytesHistogram;

    public Cqld4BaseOpDispenser(DriverAdapter adapter, LongFunction<CqlSession> sessionFunc, ParsedOp op) {
        super(adapter, op);
        this.sessionFunc = sessionFunc;
        this.maxpages = op.getStaticConfigOr("maxpages", 1);
        this.isRetryReplace = op.getStaticConfigOr("retryreplace", false);
        this.maxLwtRetries = op.getStaticConfigOr("maxlwtretries", 1);
        this.rowsHistogram = create().histogram(
            "rows",
            op.getStaticConfigOr("hdr_digits", 3),
            MetricCategory.Payload,
            "The number of rows returned in the CQL result"
        );
        this.pagesHistogram = create().histogram(
            "pages",
            op.getStaticConfigOr("hdr_digits", 3),
            MetricCategory.Payload,
            "The number of pages returned in the CQL result"
        );
        this.payloadBytesHistogram = create().histogram(
            "payload_bytes",
            op.getStaticConfigOr("hdr_digits", 3),
            MetricCategory.Payload,
            "The number of bytes returned in the CQL result"
        );
    }

    public int getMaxPages() {
        return maxpages;
    }

    public boolean isRetryReplace() {
        return isRetryReplace;
    }

    public int getMaxLwtRetries() {
        return maxLwtRetries;
    }


    public LongFunction<CqlSession> getSessionFunc() {
        return sessionFunc;
    }

    /**
     * All implementations of a CQL Statement Dispenser should be using the method
     * provided by this function. This ensures that {@link Statement}-level attributes
     * are handled uniformly and in one place.
     * <p>
     * This takes the base statement function and decorates it optionally with each
     * additional qualified modifier, short-circuiting those which are not specified.
     * This allows default behavior to take precedence as well as avoids unnecessary calling
     * overhead for implicit attributes. This should be called when the stmt function is
     * initialized within each dispenser, not for each time dispensing occurs.
     */
    protected LongFunction<Statement> getEnhancedStmtFunc(LongFunction<Statement> basefunc, ParsedOp op) {

        LongFunction<Statement> partial = basefunc;
        partial = op.enhanceEnumOptionally(partial, "cl", DefaultConsistencyLevel.class, Statement::setConsistencyLevel);
        partial = op.enhanceEnumOptionally(partial, "consistency_level", DefaultConsistencyLevel.class, Statement::setConsistencyLevel);
        partial = op.enhanceEnumOptionally(partial, "scl", DefaultConsistencyLevel.class, Statement::setSerialConsistencyLevel);
        partial = op.enhanceEnumOptionally(partial, "serial_consistency_level", DefaultConsistencyLevel.class, Statement::setSerialConsistencyLevel);
        partial = op.enhanceFuncOptionally(partial, "idempotent", Boolean.class, Statement::setIdempotent);
        partial = op.enhanceFuncOptionally(partial, "timeout", double.class, (statement, l) -> statement.setTimeout(Duration.ofMillis((long) (l * 1000L))));
        partial = op.enhanceFuncOptionally(partial, "custom_payload", Map.class, Statement::setCustomPayload);
        partial = op.enhanceFuncOptionally(partial, "execution_profile", DriverExecutionProfile.class, Statement::setExecutionProfile);
        partial = op.enhanceFuncOptionally(partial, "execution_profile_name", String.class, Statement::setExecutionProfileName);
        partial = op.enhanceFuncOptionally(partial, "node", Node.class, Statement::setNode);
        partial = op.enhanceFuncOptionally(partial, "now_in_seconds", int.class, Statement::setNowInSeconds);
        partial = op.enhanceFuncOptionally(partial, "page_size", int.class, Statement::setPageSize);
        partial = op.enhanceFuncOptionally(partial, "query_timestamp", long.class, Statement::setQueryTimestamp);
        partial = op.enhanceFuncOptionally(partial, "routing_key", ByteBuffer.class, Statement::setRoutingKey);
        partial = op.enhanceFuncOptionally(partial, "routing_keys", ByteBuffer[].class, Statement::setRoutingKey);
        partial = op.enhanceFuncOptionally(partial, "routing_token", Token.class, Statement::setRoutingToken);
        partial = op.enhanceFuncOptionally(partial, "tracing", boolean.class, Statement::setTracing);
        partial = op.enhanceFuncOptionally(partial, "showstmt", boolean.class, this::showstmt);

        return partial;
    }

    private Statement showstmt(Statement stmt, boolean input) {
        String query = cqlFor(stmt, new StringBuilder());
        logger.info(() -> "CQL(SIMPLE): " + query);
        return stmt;
    }

    private String cqlFor(Statement stmt, StringBuilder sb) {
        if (stmt instanceof SimpleStatement ss) {
            sb.append("(SIMPLE):" + ss.getQuery());
        } else if (stmt instanceof BoundStatement bs) {
            sb.append("(BOUND+" + bs.getValues().size() + " values): " + bs.getPreparedStatement().getQuery());
        } else if (stmt instanceof FluentGraphStatement fgs) {
            sb.append("(FLUENT): non-printable");
        } else if (stmt instanceof BatchStatement bs) {
            for (BatchableStatement<?> batchable : bs) {
                if (sb.length() < 1024) {
                    cqlFor(bs, sb);
                } else {
                    sb.append(("(statement too large to show)"));
                    break;
                }
            }
        } else {
            sb.append("Unknown statement type for extraction (showstmt):" + stmt.getClass().getSimpleName());
        }
        return sb.toString();
    }

    @Override
    public void recordFetchedRows(int rows) {
        rowsHistogram.update(rows);
    }

    @Override
    public void recordFetchedPages(int pages) {
        pagesHistogram.update(pages);
    }

    @Override
    public void recordFetchedBytes(int bytes) {
        payloadBytesHistogram.update(bytes);
    }
}
