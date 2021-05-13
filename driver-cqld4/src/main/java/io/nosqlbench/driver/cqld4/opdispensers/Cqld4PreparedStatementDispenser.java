package io.nosqlbench.driver.cqld4.opdispensers;

import io.nosqlbench.driver.cqld4.Cqld4Op;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.function.Function;

public class Cqld4PreparedStatementDispenser implements Function<OpTemplate, OpDispenser<Cqld4Op>> {
    public Cqld4PreparedStatementDispenser(CommandTemplate cmd) {
    }

    @Override
    public OpDispenser<Cqld4Op> apply(OpTemplate opTemplate) {
        return null;
    }
}
