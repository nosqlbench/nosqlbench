package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.util.function.LongFunction;

public class DirectOpMapper implements OpDispenser<DirectCall> {

    private final OpTemplate opTemplate;
    private final LongFunction<DirectCall> readyOp;

    public DirectOpMapper(OpTemplate opTemplate) {
        this.opTemplate = opTemplate;
        this.readyOp = resolve(opTemplate);
    }

    private LongFunction<DirectCall> resolve(OpTemplate opTemplate) {
        return new DynamicCallDispenser(opTemplate);
    }

    public DirectCall apply(long value) {
        return readyOp.apply(value);
    }
}
