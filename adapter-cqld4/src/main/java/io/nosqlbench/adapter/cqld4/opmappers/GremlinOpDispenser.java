package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement;
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatementBuilder;
import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.Optional;
import java.util.function.LongFunction;

public class GremlinOpDispenser extends BaseOpDispenser<Cqld4ScriptGraphOp> {

    private final LongFunction<? extends ScriptGraphStatement> stmtFunc;
    private final CqlSession session;
    private final LongFunction<Long> diagFunc;

    public GremlinOpDispenser(CqlSession session, ParsedOp cmd) {
        super(cmd);
        this.session = session;
        this.diagFunc = cmd.getAsFunctionOr("diag", 0L);

        LongFunction<ScriptGraphStatementBuilder> func = l -> new ScriptGraphStatementBuilder();

        // graphname
        Optional<LongFunction<String>> graphnameFunc = cmd.getAsOptionalFunction("graphname");
        if (graphnameFunc.isPresent()) {
            LongFunction<ScriptGraphStatementBuilder> finalFunc = func;
            LongFunction<String> stringLongFunction = graphnameFunc.get();
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

        // TODO: investigate enabling equivalent settings in core graph
        /**
         *            gs.setGraphInternalOption("cfg.external_vertex_verify",String.valueOf(verifyVertexIds));
         *             gs.setGraphInternalOption("cfg.verify_unique",String.valueOf(java.lang.Boolean.FALSE));
         *
         */
    }

    @Override
    public Cqld4ScriptGraphOp apply(long value) {
        ScriptGraphStatement stmt = stmtFunc.apply(value);
        if (diagFunc.apply(value)>0L) {
            System.out.println("## GREMLIN DIAG: ScriptGraphStatement on graphname(" + stmt.getGraphName() + "):\n" + stmt.getScript());
        }
        return new Cqld4ScriptGraphOp(session, stmt);
    }

}
