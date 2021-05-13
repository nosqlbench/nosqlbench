package io.nosqlbench.driver.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.driver.cqld4.Cqld4Op;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.function.Function;

public class Cqld4BatchStatementDispenser implements Function<OpTemplate, OpDispenser<Cqld4Op>> {
    private final CqlSession session;
    private final CommandTemplate cmd;

    public Cqld4BatchStatementDispenser(CqlSession session, CommandTemplate cmd) {
        this.session = session;
        this.cmd = cmd;
    }

    @Override
    public OpDispenser<Cqld4Op> apply(OpTemplate opTemplate) {
        return null;
    }
}
