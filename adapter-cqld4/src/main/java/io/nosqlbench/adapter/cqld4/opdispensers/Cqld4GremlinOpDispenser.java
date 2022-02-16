package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatementBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.Optional;
import java.util.function.LongFunction;

public class Cqld4GremlinOpDispenser extends BaseOpDispenser<Cqld4ScriptGraphOp> {

    private final LongFunction<? extends ScriptGraphStatement> stmtFunc;
    private final LongFunction<CqlSession> sessionFunc;
    private final LongFunction<Long> diagFunc;

    public Cqld4GremlinOpDispenser(LongFunction<CqlSession> sessionFunc, ParsedOp cmd) {
        super(cmd);
        this.sessionFunc = sessionFunc;
        this.diagFunc = cmd.getAsFunctionOr("diag", 0L);

        LongFunction<ScriptGraphStatementBuilder> func = l -> new ScriptGraphStatementBuilder();

        // graphname
        Optional<LongFunction<String>> graphnameFunc = cmd.getAsOptionalFunction("graphname");
        if (graphnameFunc.isPresent()) {
            LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
            LongFunction<? extends String> stringLongFunction = graphnameFunc.get();
            func = l -> finalFunc.apply(l).setGraphName(stringLongFunction.apply(l));
        }

        // script
        Optional<LongFunction<String>> scriptFunc = cmd.getAsOptionalFunction("script");
        if (scriptFunc.isPresent()) {
            LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
            func = l -> finalFunc.apply(l).setScript(scriptFunc.get().apply(l));
        }

        LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
        this.stmtFunc = l -> finalFunc.apply(l).build();

    }

    @Override
    public Cqld4ScriptGraphOp apply(long value) {
        ScriptGraphStatement stmt = stmtFunc.apply(value);
        if (diagFunc.apply(value)>0L) {
            System.out.println("## GREMLIN DIAG: ScriptGraphStatement on graphname(" + stmt.getGraphName() + "):\n" + stmt.getScript());
        }
        return new Cqld4ScriptGraphOp(sessionFunc.apply(value), stmt);
    }

}
