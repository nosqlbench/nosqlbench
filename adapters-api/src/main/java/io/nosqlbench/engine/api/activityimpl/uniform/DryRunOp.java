package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.RunnableOp;

public class DryRunOp implements RunnableOp {

    private final Op op;

    public DryRunOp(Op op) {
        this.op = op;
    }
    @Override
    public void run() {
    }
}
