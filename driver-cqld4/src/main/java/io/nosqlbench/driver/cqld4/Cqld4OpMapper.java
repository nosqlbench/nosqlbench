package io.nosqlbench.driver.cqld4;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.driver.cqld4.opdispensers.Cqld4BatchStatementDispenser;
import io.nosqlbench.driver.cqld4.opdispensers.Cqld4PreparedStatementDispenser;
import io.nosqlbench.driver.cqld4.opdispensers.Cqld4SimpleCqlStatementDispenser;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedCommand;

import java.util.function.Function;

public class Cqld4OpMapper implements Function<OpTemplate, OpDispenser<Cqld4Op>> {

    private final Function<OpTemplate, OpDispenser<Cqld4Op>> templateToDispenser;
    private final CqlSession session;

    public Cqld4OpMapper(CqlSession session, OpTemplate optpl) {
        this.session = session;
        ParsedCommand cmd = new ParsedCommand(optpl);
        templateToDispenser = resolve(cmd);
    }

    private Function<OpTemplate, OpDispenser<Cqld4Op>> resolve(ParsedCommand cmd) {
        if (cmd.isDefinedStatic("prepared") && cmd.getStaticValue("prepared", boolean.class)) {
            return new Cqld4PreparedStatementDispenser(cmd);
        } else if (cmd.isDefinedStatic("batch")) {
            return ot -> new Cqld4BatchStatementDispenser(session, cmd);
        } else {
            return ot -> new Cqld4SimpleCqlStatementDispenser(session, cmd);
        }
    }

    @Override
    public OpDispenser<Cqld4Op> apply(OpTemplate opTemplate) {
        return templateToDispenser.apply(opTemplate);

    }
}
