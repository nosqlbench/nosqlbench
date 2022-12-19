package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class DryRunOpDispenserWrapper extends BaseOpDispenser<Op, Object> {

    private final OpDispenser<? extends Op> realDispenser;

    public DryRunOpDispenserWrapper(DriverAdapter<Op,Object> adapter, ParsedOp pop, OpDispenser<? extends Op> realDispenser) {
        super(adapter, pop);
        this.realDispenser = realDispenser;
    }
    @Override
    public DryRunOp apply(long cycle) {
        Op op = realDispenser.apply(cycle);
        return new DryRunOp(op);
    }
}
