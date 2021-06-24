package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.function.Function;

public class DiagRunnableOpMapper implements Function<OpTemplate, OpDispenser<Runnable>> {

    @Override
    public OpDispenser<Runnable> apply(OpTemplate optpl) {
        CommandTemplate commandTemplate = new CommandTemplate(optpl);
        return new DiagRunnableOpDispenser(commandTemplate);
    }

}
