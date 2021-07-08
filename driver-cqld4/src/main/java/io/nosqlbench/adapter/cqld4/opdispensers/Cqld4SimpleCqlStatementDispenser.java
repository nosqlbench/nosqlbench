package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.driver.cqld4.Cqld4Op;
import io.nosqlbench.adapter.optypes.Cqld4SimpleCqlStatement;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;

public class Cqld4SimpleCqlStatementDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedCommand cmd;

    public Cqld4SimpleCqlStatementDispenser(CqlSession session, ParsedCommand cmd) {
        this.session = session;
        this.cmd = cmd;
    }

    @Override
    public Cqld4SimpleCqlStatement apply(long value) {
        String stmtBody = cmd.get("stmt",value);
        SimpleStatement simpleStatement = SimpleStatement.newInstance(stmtBody);
        return new Cqld4SimpleCqlStatement(session,simpleStatement);
    }
}
