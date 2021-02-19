package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "cqlverify")
public class CqlVerifyActivityType implements ActivityType<CqlVerifyActivity> {

    @Override
    public ActionDispenser getActionDispenser(CqlVerifyActivity activity) {
        return new CqlVerifyActionDispenser(activity);
    }

    @Override
    public CqlVerifyActivity getActivity(ActivityDef activityDef) {
        return new CqlVerifyActivity(activityDef);
    }
}
