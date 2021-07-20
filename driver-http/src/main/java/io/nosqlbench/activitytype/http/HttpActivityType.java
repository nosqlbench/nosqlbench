package io.nosqlbench.activitytype.http;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = ActivityType.class, selector = "http")
public class HttpActivityType implements ActivityType<HttpActivity> {

    private static final Logger logger = LogManager.getLogger(HttpActivityType.class);

    @Override
    public ActionDispenser getActionDispenser(HttpActivity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("The async http driver is not online yet.");
        }
        return new HttpActionDispenser(activity);
    }

    @Override
    public HttpActivity getActivity(ActivityDef activityDef) {
        return new HttpActivity(activityDef);
    }
}
