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
    private PreparedStatement preparedStmt;
    private CqlSession boundSession;

    public Cqld4PreparedStmtDispenser(LongFunction<CqlSession> sessionFunc, ParsedOp cmd, RSProcessors processors) {
        super(sessionFunc, cmd);
        if (cmd.isDynamic("space")) {
            throw new RuntimeException("Prepared statements and dynamic space values are not supported." +
                " This would churn the prepared statement cache, defeating the purpose of prepared statements.");
        }
        this.processors = processors;
        stmtFunc = super.getStmtFunc();
    }

    @Override
    protected LongFunction<Statement> getPartialStmtFunction(ParsedOp cmd) {

        LongFunction<Object[]> varbinder;
        ParsedTemplate parsed = cmd.getStmtAsTemplate().orElseThrow();
        varbinder = cmd.newArrayBinderFromBindPoints(parsed.getBindPoints());
        String preparedQueryString = parsed.getPositionalStatement(s -> "?");
        boundSession = getSessionFunc().apply(0);
        preparedStmt = boundSession.prepare(preparedQueryString);

        LongFunction<Statement> boundStmtFunc = c -> {
            Object[] apply = varbinder.apply(c);
            return preparedStmt.bind(apply);
        };
        return boundStmtFunc;
    }

    @Override
    public Cqld4CqlOp apply(long value) {

        return new Cqld4CqlPreparedStatement(
            boundSession,
            (BoundStatement) getStmtFunc().apply(value),
            getMaxPages(),
            isRetryReplace(),
            processors
        );
    }

}
