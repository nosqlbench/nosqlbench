package io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes;

public interface RunnableOp extends CycleOp<Void>,Runnable {

    @Override
    default Void apply(long value) {
        run();
        return null;
    }
}
