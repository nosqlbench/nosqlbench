package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class Cqld4SimpleCqlStmtDispenser extends BaseCqlStmtDispenser {

    private final LongFunction<Statement> stmtFunc;
    private final LongFunction<String> targetFunction;

    public Cqld4SimpleCqlStmtDispenser(LongFunction<CqlSession> sessionFunc, LongFunction<String> targetFunction, ParsedOp cmd) {
        super(sessionFunc,cmd);
        this.targetFunction=targetFunction;
        this.stmtFunc =createStmtFunc(cmd);
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp op) {
        return super.getEnhancedStmtFunc(l -> SimpleStatement.newInstance(targetFunction.apply(l)),op);
    }

    @Override
    public Cqld4CqlSimpleStatement apply(long value) {
        return new Cqld4CqlSimpleStatement(
            getSessionFunc().apply(value),
            (SimpleStatement) stmtFunc.apply(value),
            getMaxPages(),
            isRetryReplace()
        );
    }

}
