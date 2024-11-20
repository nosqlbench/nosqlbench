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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.diagnostics.CQLD4PreparedStmtDiagnostics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlPreparedStatement;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class Cqld4PreparedStmtDispenser extends Cqld4BaseOpDispenser<Cqld4CqlPreparedStatement> {
    private final static Logger logger = LogManager.getLogger(Cqld4PreparedStmtDispenser.class);

    private final RSProcessors processors;
    private final LongFunction<Statement> stmtFunc;
    private final ParsedTemplateString stmtTpl;
    private final LongFunction<Object[]> fieldsF;
    private final LongFunction<Cqld4Space> spaceInitF;
    private PreparedStatement preparedStmt;
    // This is a stable enum for the op template from the workload, bounded by cardinality of all op templates
    private int refkey;

    public Cqld4PreparedStmtDispenser(
        Cqld4DriverAdapter adapter,
        ParsedOp op,
        ParsedTemplateString stmtTpl,
        RSProcessors processors,
        LongFunction<Cqld4Space> spaceInitF
    ) {
        super(adapter, op);
        this.processors = processors;
        this.stmtTpl = stmtTpl;
        this.fieldsF = getFieldsFunction(op);
        this.spaceInitF = spaceInitF;
        stmtFunc = createStmtFunc(fieldsF, op);
    }

    private LongFunction<Object[]> getFieldsFunction(ParsedOp op) {
        LongFunction<Object[]> varbinder;
        varbinder = op.newArrayBinderFromBindPoints(stmtTpl.getBindPoints());
        this.refkey = op.getRefKey();
        return varbinder;
    }


    protected LongFunction<Statement> createStmtFunc(LongFunction<Object[]> fieldsF, ParsedOp op) {

        try {
            String preparedQueryString = stmtTpl.getPositionalStatement(s -> "?");

            LongFunction<PreparedStatement> prepareStatementF =
                (long l) -> (sessionF.apply(l)).prepare(preparedQueryString);

            LongFunction<? extends Cqld4Space> lookupSpaceF =
                (long l) -> spaceInitF.apply(l);

            int refKey = op.getRefKey();
            LongFunction<PreparedStatement> cachedStatementF =
                (long l) -> lookupSpaceF.apply(l).getOrCreatePreparedStatement(refKey,prepareStatementF);

            LongFunction<Statement> boundStatementF =
                (long l) -> cachedStatementF.apply(l).bind(fieldsF.apply(l));

            return super.getEnhancedStmtFunc(boundStatementF, op);

        } catch (Exception e) {
            throw new OpConfigError(e + "( for statement '" + stmtTpl + "')");
        }

    }

    @Override
    public Cqld4CqlPreparedStatement getOp(long cycle) {
        BoundStatement stmt = (BoundStatement) stmtFunc.apply(cycle);
        try {
            CqlSession session = (CqlSession) sessionF.apply(cycle);
            return new Cqld4CqlPreparedStatement(
                sessionF.apply(cycle),
                stmt,
                getMaxPages(),
                isRetryReplace(),
                getMaxLwtRetries(),
                processors,
                this
            );
        } catch (Exception exception) {
            return CQLD4PreparedStmtDiagnostics.rebindWithDiagnostics(
                preparedStmt,
                fieldsF,
                cycle,
                exception
            );
        }
    }
}
