package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.activitytype.cql.core.CqlAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;

public class CqlVerifyAsyncAction extends CqlAsyncAction implements ActivityDefObserver {

    public CqlVerifyAsyncAction(ActivityDef activityDef, int slot, CqlActivity cqlActivity) {
        super(cqlActivity, slot);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }


}
