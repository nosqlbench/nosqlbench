package io.nosqlbench.activitytype.cql.ebdrivers.cql.core;


import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;

public class CqlActionDispenser implements ActionDispenser {

    public CqlActivity getCqlActivity() {
        return cqlActivity;
    }

    private CqlActivity cqlActivity;

    public CqlActionDispenser(CqlActivity activityContext) {
        this.cqlActivity = activityContext;
    }

    public Action getAction(int slot) {
        long async= cqlActivity.getActivityDef().getParams().getOptionalLong("async").orElse(0L);
        if (async>0) {
            return new CqlAsyncAction(cqlActivity, slot);
        } else {
            return new CqlAction(cqlActivity.getActivityDef(), slot, cqlActivity);
        }
    }
}
