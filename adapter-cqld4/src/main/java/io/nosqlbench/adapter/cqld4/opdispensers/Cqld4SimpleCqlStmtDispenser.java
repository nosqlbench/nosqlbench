package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4SimpleCqlStatement;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class Cqld4SimpleCqlStmtDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedOp cmd;
    private final int maxpages;
    private final boolean retryreplace;
    private final Cqld4OpMetrics metrics;

    public Cqld4SimpleCqlStmtDispenser(CqlSession session, ParsedOp cmd) {
        this.session = session;
        this.cmd = cmd;
        this.maxpages = cmd.getStaticConfigOr("maxpages",1);
        this.retryreplace = cmd.getStaticConfigOr("retryreplace",false);
        this.metrics = new Cqld4OpMetrics();
    }

    @Override
    public Cqld4SimpleCqlStatement apply(long value) {
        String stmtBody = cmd.get("stmt",value);
        SimpleStatement simpleStatement = SimpleStatement.newInstance(stmtBody);
        return new Cqld4SimpleCqlStatement(session,simpleStatement,maxpages,retryreplace,metrics);
    }
}
