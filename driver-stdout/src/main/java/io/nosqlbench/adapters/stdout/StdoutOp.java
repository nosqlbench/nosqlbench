package io.nosqlbench.adapters.stdout;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.RunnableOp;

public class StdoutOp implements RunnableOp {

    private final StdoutSpace ctx;
    private final String text;

    public StdoutOp(StdoutSpace ctx, String text) {
        this.ctx = ctx;
        this.text = text;
    }

    @Override
    public void run() {
        ctx.write(text);
    }
}
