package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4PreparedStatement;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.function.LongFunction;

public class Cqld4PreparedOpDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedCommand cmd;
    private final NBConfiguration cfg;

    private final LongFunction<Object[]> varbinder;
    private final PreparedStatement preparedStmt;

    public Cqld4PreparedOpDispenser(CqlSession session, ParsedCommand cmd, NBConfiguration cfg) {
        this.session = session;
        this.cmd = cmd;
        this.cfg = cfg;
//        if (cmd.isDefinedDynamic("stmt")) {
//            throw new OpConfigError("You must have a static template to create prepared statements. (Do not make the stmt field a binding itself)");
//        }
        ParsedTemplate parsed = cmd.getStmtAsTemplate().orElseThrow();
        String preparedQueryString = parsed.getPositionalStatement(s -> "?");

        varbinder = cmd.newArrayBinderFromBindPoints(parsed.getBindPoints());
        preparedStmt = session.prepare(preparedQueryString);

    }

    // TODO: Explain in the dev guide that apply in the op dispenser should do all the "bind" level stuff
    @Override
    public Cqld4Op apply(long value) {
        Object[] parameters = varbinder.apply(value);
        BoundStatement stmt = preparedStmt.bind(parameters);
        return new Cqld4PreparedStatement(session, stmt);
    }
}
