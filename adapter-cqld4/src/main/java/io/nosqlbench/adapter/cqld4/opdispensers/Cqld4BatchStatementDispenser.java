package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

public class Cqld4BatchStatementDispenser implements OpDispenser<Cqld4Op> {
    private final CqlSession session;
    private final ParsedCommand cmd;
    private final NBConfiguration cfg;

    public Cqld4BatchStatementDispenser(CqlSession session, ParsedCommand cmd, NBConfiguration cfg) {
        this.session = session;
        this.cmd = cmd;
        this.cfg = cfg;
    }

    @Override
    public Cqld4Op apply(long value) {
        return null;
    }

}
