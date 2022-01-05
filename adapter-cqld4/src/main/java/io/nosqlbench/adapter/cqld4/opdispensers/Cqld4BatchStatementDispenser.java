package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class Cqld4BatchStatementDispenser extends BaseOpDispenser<Cqld4CqlOp> {
    private final CqlSession session;
    private final ParsedOp cmd;

    public Cqld4BatchStatementDispenser(CqlSession session, ParsedOp cmd) {
        super(cmd);
        this.session = session;
        this.cmd = cmd;
    }

    @Override
    public Cqld4CqlOp apply(long value) {
        return null;
    }

}
