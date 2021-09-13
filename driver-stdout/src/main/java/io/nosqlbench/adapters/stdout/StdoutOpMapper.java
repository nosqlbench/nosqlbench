package io.nosqlbench.adapters.stdout;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StdoutOpMapper implements OpMapper<StdoutOp> {

    private final DriverSpaceCache<? extends StdoutSpace> ctxcache;

    public StdoutOpMapper(DriverSpaceCache<? extends StdoutSpace> ctxcache) {
        this.ctxcache = ctxcache;
    }

    @Override
    public OpDispenser<StdoutOp> apply(ParsedOp cmd) {
        LongFunction<String> spacefunc = cmd.getAsFunctionOr("space", "default");
        LongFunction<StdoutSpace> ctxfunc = (cycle) -> ctxcache.get(spacefunc.apply(cycle));
        return new StdoutOpDispenser(cmd,ctxfunc);
    }

}
