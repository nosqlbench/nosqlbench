package io.nosqlbench.activitytype.cqlverify;

import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.activitytype.cql.statements.rsoperators.AssertSingleRowResultSet;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This activity is just a thin wrapper at this point.
 * Most of the functionality it used to have has been
 * generalized into the cql activity proper at this point.
 */
public class CqlVerifyActivity extends CqlActivity {

    private final static Logger logger = LogManager.getLogger(CqlVerifyActivity.class);

    public CqlVerifyActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public synchronized void initActivity() {

        if (!super.getActivityDef().getParams().contains("verify") &&
            !super.getActivityDef().getParams().contains("verify-fields")) {
            logger.info("Pre-configuring activity param 'verify=*' since none was provided.");
            logger.info("To control this on a per-statement basis, use the verify param.");
            super.getActivityDef().getParams().put("verify", "*");
        }

        if (!super.getActivityDef().getParams().contains("compare")) {
            super.getActivityDef().getParams().put("compare", "all");
            logger.info("Pre-configuring activity param 'compare=all' since none was provided.");
            logger.info("To control this on a per-statement basis, use the compare param.");
        }

        super.initActivity();

    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
        addResultSetCycleOperator(new AssertSingleRowResultSet());

    }
}
