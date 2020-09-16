package io.nosqlbench.engine.api.activityimpl.input;

import io.nosqlbench.engine.api.activityapi.core.RunState;

public interface StateCapable {
    RunState getRunState();
}
