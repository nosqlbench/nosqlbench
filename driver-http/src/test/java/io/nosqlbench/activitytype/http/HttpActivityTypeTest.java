package io.nosqlbench.activitytype.http;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.junit.Test;

public class HttpActivityTypeTest {
    @Test
    public void testHttpActivity() {
        HttpActivityType httpAt = new HttpActivityType();

        ActivityDef ad = ActivityDef.parseActivityDef("driver=http; yaml=http-google.yaml; port=80; cycles=1;");
        HttpActivity httpActivity = httpAt.getActivity(ad);
        httpActivity.initActivity();
        ActionDispenser actionDispenser = httpAt.getActionDispenser(httpActivity);
        Action action = actionDispenser.getAction(1);
    }
}
