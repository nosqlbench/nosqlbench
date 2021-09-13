package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;

public class StandardActivityType<A extends StandardActivity<?,?>> extends SimpleActivity implements ActivityType<A> {

    private final DriverAdapter<?,?> adapter;

    public StandardActivityType(DriverAdapter<?,?> adapter, ActivityDef activityDef) {
        super(activityDef);
        this.adapter = adapter;
        if (adapter instanceof ActivityDefAware) {
            ((ActivityDefAware) adapter).setActivityDef(activityDef);
        }
    }

    @Override
    public A getActivity(ActivityDef activityDef) {
        if (activityDef.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("This driver does not support async mode yet.");
        }

        return (A) new StandardActivity(adapter,activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(A activity) {
        return new StandardActionDispenser(activity);
    }


}
