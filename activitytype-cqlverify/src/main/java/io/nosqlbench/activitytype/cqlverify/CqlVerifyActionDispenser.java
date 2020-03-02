package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.activitytype.cql.ebdrivers.cql.core.CqlActionDispenser;
import io.nosqlbench.activitytype.cql.ebdrivers.cql.core.CqlActivity;
import io.nosqlbench.engine.api.activityapi.core.Action;

public class CqlVerifyActionDispenser extends CqlActionDispenser {
    public CqlVerifyActionDispenser(CqlActivity cqlActivity) {
        super(cqlActivity);
    }

    public Action getAction(int slot) {
        long async= getCqlActivity().getActivityDef().getParams().getOptionalLong("async").orElse(0L);
        if (async>0) {
            return new CqlVerifyAsyncAction(getCqlActivity().getActivityDef(), slot, getCqlActivity());
        } else {
            return new CqlVerifyAction(getCqlActivity().getActivityDef(), slot, getCqlActivity());
        }
    }

}
