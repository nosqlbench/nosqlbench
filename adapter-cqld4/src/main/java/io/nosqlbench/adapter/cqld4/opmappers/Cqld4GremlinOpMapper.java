package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4GremlinOpDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class Cqld4GremlinOpMapper implements OpMapper<Cqld4ScriptGraphOp> {
    private final LongFunction<CqlSession> sessionFunc;
    private final LongFunction<String> targetFunction;

    public Cqld4GremlinOpMapper(LongFunction<CqlSession> session, LongFunction<String> targetFunction) {
        this.sessionFunc = session;
        this.targetFunction = targetFunction;
    }

    public OpDispenser<Cqld4ScriptGraphOp> apply(ParsedOp cmd) {
        return new Cqld4GremlinOpDispenser(sessionFunc, targetFunction, cmd);
    }
}
