/*
 * Copyright (c) 2022 nosqlbench
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
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlPreparedStatement;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.function.LongFunction;

public class Cqld4PreparedStmtDispenser extends BaseCqlStmtDispenser {

    private final RSProcessors processors;
    private final LongFunction<Statement> stmtFunc;
    private final ParsedTemplate stmtTpl;
    private PreparedStatement preparedStmt;
    private CqlSession boundSession;

    public Cqld4PreparedStmtDispenser(LongFunction<CqlSession> sessionFunc, ParsedOp cmd, ParsedTemplate stmtTpl, RSProcessors processors) {
        super(sessionFunc, cmd);
        if (cmd.isDynamic("space")) {
            throw new RuntimeException("Prepared statements and dynamic space values are not supported." +
                " This would churn the prepared statement cache, defeating the purpose of prepared statements.");
        }
        this.processors = processors;
        this.stmtTpl = stmtTpl;
        stmtFunc = createStmtFunc(cmd);
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp cmd) {

        LongFunction<Object[]> varbinder;
        varbinder = cmd.newArrayBinderFromBindPoints(stmtTpl.getBindPoints());
        String preparedQueryString = stmtTpl.getPositionalStatement(s -> "?");
        boundSession = getSessionFunc().apply(0);
        preparedStmt = boundSession.prepare(preparedQueryString);

        LongFunction<Statement> boundStmtFunc = c -> {
            Object[] apply = varbinder.apply(c);
            return preparedStmt.bind(apply);
        };
        return super.getEnhancedStmtFunc(boundStmtFunc, cmd);
    }

    @Override
    public Cqld4CqlOp apply(long value) {

        return new Cqld4CqlPreparedStatement(
            boundSession,
            (BoundStatement) stmtFunc.apply(value),
            getMaxPages(),
            isRetryReplace(),
            processors
        );
    }

}
