package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.activitytype.cql.ebdrivers.cql.core.CqlAction;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.core.CqlActivity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

public class CqlVerifyAction extends CqlAction implements ActivityDefObserver {

    public CqlVerifyAction(ActivityDef activityDef, int slot, CqlActivity cqlActivity) {
        super(activityDef, slot, cqlActivity);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }


}
