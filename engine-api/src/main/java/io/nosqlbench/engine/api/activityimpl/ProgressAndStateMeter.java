package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.api.activityimpl.input.StateCapable;

public class ProgressAndStateMeter implements ProgressMeter, StateCapable {
    private final ProgressMeter meter;
    private final StateCapable statesrc;

    public ProgressAndStateMeter(ProgressMeter meter, StateCapable statesrc) {
        this.meter = meter;
        this.statesrc = statesrc;
    }

    @Override
    public String getProgressName() {
        return meter.getProgressName();
    }

    @Override
    public long getStartedAtMillis() {
        return meter.getStartedAtMillis();
    }

    @Override
    public long getProgressMin() {
        return meter.getProgressMin();
    }

    @Override
    public long getProgressCurrent() {
        return meter.getProgressCurrent();
    }

    @Override
    public long getProgressMax() {
        return meter.getProgressMax();
    }

    @Override
    public long getRecyclesCurrent() {
        return meter.getRecyclesCurrent();
    }

    @Override
    public long getRecyclesMax() {
        return meter.getRecyclesMax();
    }

    @Override
    public RunState getRunState() {
        return statesrc.getRunState();
    }
}
