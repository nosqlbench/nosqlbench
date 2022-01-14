package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class Cqld4GremlinOpMapper implements OpMapper<Cqld4ScriptGraphOp> {
    private final CqlSession session;

    public Cqld4GremlinOpMapper(CqlSession session) {
        this.session = session;
    }

    public OpDispenser<Cqld4ScriptGraphOp> apply(ParsedOp cmd) {
        return new GremlinOpDispenser(session, cmd);
    }
}
