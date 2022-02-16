package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class Cqld4RawStmtDispenser extends BaseCqlStmtDispenser {

    private final LongFunction<Statement> stmtFunc;
    private final LongFunction<String> targetFunction;

    public Cqld4RawStmtDispenser(LongFunction<CqlSession> sessionFunc, LongFunction<String> targetFunction, ParsedOp cmd) {
        super(sessionFunc, cmd);
        this.targetFunction=targetFunction;
        this.stmtFunc = createStmtFunc(cmd);
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp cmd) {
        LongFunction<Statement> basefunc = l -> new SimpleStatementBuilder(targetFunction.apply(l)).build();
        return super.getEnhancedStmtFunc(basefunc,cmd);
    }

    @Override
    public Cqld4CqlOp apply(long value) {
        return new Cqld4CqlSimpleStatement(
            getSessionFunc().apply(value),
            (SimpleStatement) stmtFunc.apply(value),
            getMaxPages(),
            isRetryReplace()
        );
    }

}
