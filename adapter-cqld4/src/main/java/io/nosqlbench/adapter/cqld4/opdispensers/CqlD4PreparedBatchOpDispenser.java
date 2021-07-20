package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;

public class CqlD4PreparedBatchOpDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedCommand cmd;

    public CqlD4PreparedBatchOpDispenser(CqlSession session, ParsedCommand cmd) {
        this.session = session;
        this.cmd = cmd;
    }

    @Override
    public Cqld4Op apply(long value) {
        return null;
    }
}
