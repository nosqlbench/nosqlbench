package io.nosqlbench.adapters.stdout;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StdoutOpDispenser implements OpDispenser<StdoutOp> {

    private final LongFunction<StdoutSpace> ctxfunc;
    private final LongFunction<?> objectFunction;

    public StdoutOpDispenser(ParsedOp cmd, LongFunction<StdoutSpace> ctxfunc) {
        objectFunction = cmd.getAsRequiredFunction("stmt", Object.class);
        this.ctxfunc = ctxfunc;
    }

    @Override
    public StdoutOp apply(long value) {
        StdoutSpace ctx = ctxfunc.apply(value);
        String output = objectFunction.apply(value).toString();
        return new StdoutOp(ctx,output);
    }
}
