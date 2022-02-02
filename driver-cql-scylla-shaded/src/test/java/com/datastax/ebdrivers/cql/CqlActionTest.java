package com.datastax.ebdrivers.cql;

import io.nosqlbench.activitytype.cql.core.CqlAction;
import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CqlActionTest {

    @Test
    @Disabled
    public void testCqlAction() {
        ActivityDef ad = ActivityDef.parseActivityDef("driver=ebdrivers;alias=foo;yaml=write-telemetry.yaml;");
        CqlActivity cac = new CqlActivity(ad);
        CqlAction cq = new CqlAction(ad, 0, cac);
        cq.init();
        cq.runCycle(5);
    }


}
