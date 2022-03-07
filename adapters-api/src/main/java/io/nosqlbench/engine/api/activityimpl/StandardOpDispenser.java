package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StandardOpDispenser<T extends Op> extends BaseOpDispenser<T>{

    private final LongFunction<T> opfunc;

    public StandardOpDispenser(ParsedOp op, LongFunction<T> opfunc) {
        super(op);
        this.opfunc = opfunc;
    }

    @Override
    public T apply(long value) {
        return opfunc.apply(value);
    }
}
