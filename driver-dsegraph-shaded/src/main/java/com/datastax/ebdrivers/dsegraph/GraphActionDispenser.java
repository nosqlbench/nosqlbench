package com.datastax.ebdrivers.dsegraph;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class GraphActionDispenser implements ActionDispenser {

    private GraphActivity activity;

    public GraphActionDispenser(GraphActivity activity) {
        this.activity = activity;
    }

    @Override
    public Action getAction(int slot) {
        return new GraphAction(slot, activity);
    }
}
