package io.nosqlbench.adapters.stdout;

import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StdoutOpDispenser extends BaseOpDispenser<StdoutOp> {

    private final LongFunction<StdoutSpace> ctxfunc;
    private final LongFunction<?> objectFunction;

    public StdoutOpDispenser(ParsedOp cmd, LongFunction<StdoutSpace> ctxfunc) {
        super(cmd);
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
