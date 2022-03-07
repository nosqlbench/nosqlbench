package io.nosqlbench.adapters.stdout;

import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StdoutOpDispenser extends BaseOpDispenser<StdoutOp> {

    private final LongFunction<StdoutSpace> ctxfunc;
    private final LongFunction<String> outFunction;

    public StdoutOpDispenser(ParsedOp cmd, LongFunction<StdoutSpace> ctxfunc) {
        super(cmd);
        this.ctxfunc = ctxfunc;
        LongFunction<Object> objectFunction = cmd.getAsRequiredFunction("stmt", Object.class);
        LongFunction<String> stringfunc = l -> objectFunction.apply(l).toString();
        cmd.enhance(stringfunc,"suffix",String.class,(a,b) -> a+b);
        this.outFunction = stringfunc;
    }

    @Override
    public StdoutOp apply(long value) {
        StdoutSpace ctx = ctxfunc.apply(value);
        String output = outFunction.apply(value);
        return new StdoutOp(ctx,output);
    }
}
