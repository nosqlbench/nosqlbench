package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;

import java.util.function.LongFunction;

public class DynamicCallDispenser implements LongFunction<DirectCall> {

    public DynamicCallDispenser(OpTemplate opTemplate) {
    }

    @Override
    public DirectCall apply(long value) {
        return null;
    }
}
