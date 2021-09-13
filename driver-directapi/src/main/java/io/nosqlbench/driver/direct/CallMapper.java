package io.nosqlbench.driver.direct;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;

public class CallMapper implements OpDispenser<DirectCall> {

    @Override
    public DirectCall apply(long value) {
        return null;
    }
}
