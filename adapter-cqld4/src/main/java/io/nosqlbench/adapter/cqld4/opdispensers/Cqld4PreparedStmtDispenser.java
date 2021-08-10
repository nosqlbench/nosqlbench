package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4PreparedStatement;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.function.LongFunction;

public class Cqld4PreparedStmtDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;

    private final LongFunction<Object[]> varbinder;
    private final PreparedStatement preparedStmt;
    private final int maxpages;
    private final boolean retryreplace;
    private final Cqld4OpMetrics metrics;
    private final RSProcessors processors;

    public Cqld4PreparedStmtDispenser(CqlSession session, ParsedCommand cmd, RSProcessors processors) {
        this.session = session;
        this.processors = processors;

        ParsedTemplate parsed = cmd.getStmtAsTemplate().orElseThrow();
        varbinder = cmd.newArrayBinderFromBindPoints(parsed.getBindPoints());

        String preparedQueryString = parsed.getPositionalStatement(s -> "?");
        preparedStmt = session.prepare(preparedQueryString);

        this.maxpages = cmd.getStaticConfigOr("maxpages",1);
        this.retryreplace = cmd.getStaticConfigOr("retryreplace", false);
        this.metrics = new Cqld4OpMetrics();
    }

    @Override
    public Cqld4Op apply(long value) {
        Object[] parameters = varbinder.apply(value);
        BoundStatement stmt = preparedStmt.bind(parameters);
        return new Cqld4PreparedStatement(session, stmt, maxpages, retryreplace, metrics, processors);
    }
}
