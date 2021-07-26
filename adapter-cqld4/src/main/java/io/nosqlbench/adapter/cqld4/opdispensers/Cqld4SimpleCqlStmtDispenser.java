package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4SimpleCqlStatement;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

public class Cqld4SimpleCqlStmtDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedCommand cmd;
    private final NBConfiguration cfg;

    public Cqld4SimpleCqlStmtDispenser(CqlSession session, ParsedCommand cmd, NBConfiguration cfg) {
        this.session = session;
        this.cmd = cmd;
        this.cfg = cfg;
    }

    @Override
    public Cqld4SimpleCqlStatement apply(long value) {
        String stmtBody = cmd.get("stmt",value);
        SimpleStatement simpleStatement = SimpleStatement.newInstance(stmtBody);
        return new Cqld4SimpleCqlStatement(session,simpleStatement);
    }
}
