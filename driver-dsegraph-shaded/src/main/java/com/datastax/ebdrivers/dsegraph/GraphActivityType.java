package com.datastax.ebdrivers.dsegraph;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(ActivityType.class)
public class GraphActivityType implements ActivityType<GraphActivity> {

    @Override
    public String getName() {
        return "dsegraph";
    }

    @Override
    public GraphActivity getActivity(ActivityDef activityDef) {
        return new GraphActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(GraphActivity activity) {
        return new GraphActionDispenser(activity);
    }
}
