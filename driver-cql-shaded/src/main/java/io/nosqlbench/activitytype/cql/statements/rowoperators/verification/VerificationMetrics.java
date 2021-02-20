package io.nosqlbench.activitytype.cql.statements.rowoperators.verification;

import com.codahale.metrics.Counter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;

public class VerificationMetrics {

    public final Counter verifiedRowsCounter;
    public final Counter unverifiedRowsCounter;
    public final Counter verifiedValuesCounter;
    public final Counter unverifiedValuesCounter;

    public VerificationMetrics(ActivityDef activityDef) {
        verifiedRowsCounter = ActivityMetrics.counter(activityDef,"verifiedrows");
        unverifiedRowsCounter= ActivityMetrics.counter(activityDef,"unverifiedrows");
        verifiedValuesCounter = ActivityMetrics.counter(activityDef,"verifiedvalues");
        unverifiedValuesCounter = ActivityMetrics.counter(activityDef,"unverifiedvalues");
    }

}
