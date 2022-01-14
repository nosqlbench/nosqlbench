package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

import java.util.function.LongFunction;

public class StandardCycleOp<T> implements CycleOp<T> {

    private final LongFunction<? extends T> opfunc;

    public StandardCycleOp(LongFunction<? extends T> opfunc) {
        this.opfunc = opfunc;
    }

    @Override
    public T apply(long value) {
        return opfunc.apply(value);
    }
}
