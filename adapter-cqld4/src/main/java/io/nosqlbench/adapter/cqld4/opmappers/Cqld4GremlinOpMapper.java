package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class Cqld4GremlinOpMapper implements OpMapper<Cqld4GremlinOp> {
    private final CqlSession session;

    public Cqld4GremlinOpMapper(CqlSession session) {
        this.session = session;
    }

    public OpDispenser<Cqld4GremlinOp> apply(ParsedOp cmd) {
        return new GremlinOpDispenser(session, cmd);
    }
}
